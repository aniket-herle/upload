package com.aniket.mirror.upload.service.impl;


import com.aniket.mirror.events.FileUploadEvent;
import com.aniket.mirror.upload.constants.enums.FileUploadStatus;
import com.aniket.mirror.upload.dto.CompleteUploadRequest;
import com.aniket.mirror.upload.dto.CreateUploadRequest;
import com.aniket.mirror.upload.dto.CreateUploadResponse;
import com.aniket.mirror.upload.entity.FileMetadata;
import com.aniket.mirror.upload.repository.FileMetaDataRepository;
import com.aniket.mirror.upload.service.KafkaProducerService;
import com.aniket.mirror.upload.service.S3FileUploadService;
import com.aniket.mirror.upload.util.FileUploadUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3FileUploadServiceImpl implements S3FileUploadService {

  private final S3Presigner s3Presigner;

  private final S3Client s3Client;

  private final FileMetaDataRepository fileMetaDataRepository;

  private final KafkaProducerService kafkaProducerService;

  @Value("${aws.s3.bucket}")
  private String bucket;

  public CreateUploadResponse createUpload(CreateUploadRequest req) {

    String id = UUID.randomUUID().toString();
    String key = "uploads/" + id + "_" + req.getFileName();

    // 1️⃣ Store metadata first
    FileMetadata meta = new FileMetadata();
    meta.setId(id);
    meta.setFileName(req.getFileName());
    meta.setS3Key(key);
    meta.setS3Bucket(bucket);
    meta.setContentType(req.getContentType());
    meta.setSizeBytes(req.getSizeBytes());
    meta.setStatus(FileUploadStatus.PENDING);
    meta.setCreatedAt(Instant.now());

    fileMetaDataRepository.save(meta);

  PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(req.getContentType())
        .build();

    PresignedPutObjectRequest preSigned =
        s3Presigner.presignPutObject(r -> r
            .signatureDuration(Duration.ofMinutes(10))
            .putObjectRequest(putObjectRequest)
        );


    CreateUploadResponse res = new CreateUploadResponse();
    res.setFileId(meta.getId());
    res.setUploadUrl(preSigned.url().toString());
    res.setS3Key(key);
    return res;
  }

  public void completeUpload(String fileId, CompleteUploadRequest req) {

    FileMetadata meta = fileMetaDataRepository.findById(fileId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid fileId"));

    // 1️⃣ Idempotency Commented out for testing
//    if (meta.getStatus() == FileUploadStatus.UPLOADED) {
//      return; // already completed
//    }

//    // 2️⃣ Only PENDING allowed
//    if (meta.getStatus() != FileUploadStatus.PENDING) {
//      throw new IllegalStateException(
//          "Cannot complete upload in state: " + meta.getStatus());
//    }

    // 3️⃣ Verify object exists in S3
    HeadObjectResponse head;
    try {
      head = s3Client.headObject(
          HeadObjectRequest.builder()
              .bucket(meta.getS3Bucket())
              .key(meta.getS3Key())
              .build()
      );
    } catch (NoSuchKeyException e) {
      throw new IllegalStateException("File not found in S3");
    } catch (S3Exception e) {

    if (e.statusCode() == 403) {
      // Access denied
      throw new RuntimeException("You do not have permission to access this file/the file doesn't exist", e);
    }

    throw e; // rethrow unknown S3 errors
  }

    // 4️⃣ Size validation (CRITICAL)
    if (!Objects.equals(head.contentLength(), meta.getSizeBytes())) {
      throw new IllegalStateException(
          "Uploaded size mismatch. Expected=" +
              meta.getSizeBytes() + ", actual=" + head.contentLength());
    }

    // 5️⃣ Optional checksum
    if (req != null && req.getChecksum() != null) {
      if(req.getChecksum().equals(meta.getChecksum())) {
        meta.setChecksum(head.checksumSHA256());
      }else{
        throw new IllegalStateException("Checksum mismatch");
      }
    }else{
      meta.setChecksum(head.checksumSHA256());
    }

    // 6️⃣ Finalize
    meta.setStatus(FileUploadStatus.UPLOADED);
    meta.setS3Url("s3://" + meta.getS3Bucket() + "/" + meta.getS3Key());

    fileMetaDataRepository.save(meta);

    FileUploadEvent fileUploadEvent = FileUploadUtil.getFileUploadEvent(meta);
    kafkaProducerService.sendFileUploadEvent(fileUploadEvent);
  }

}
