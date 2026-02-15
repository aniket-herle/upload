package com.aniket.mirror.upload.service.impl;

import com.aniket.mirror.events.FileUploadEvent;
import com.aniket.mirror.upload.exception.ClientException;
import com.aniket.mirror.upload.exception.ErrorCode;
import com.aniket.mirror.upload.exception.ServerException;
import com.aniket.mirror.upload.constants.enums.FileUploadStatus;
import com.aniket.mirror.upload.dto.CompleteUploadRequest;
import com.aniket.mirror.upload.dto.CreateUploadRequest;
import com.aniket.mirror.upload.dto.CreateUploadResponse;
import com.aniket.mirror.upload.entity.FileRecord;
import com.aniket.mirror.upload.entity.User;
import com.aniket.mirror.upload.repository.FileRecordRepository;
import com.aniket.mirror.upload.service.KafkaProducerService;
import com.aniket.mirror.upload.service.S3FileUploadService;
import com.aniket.mirror.upload.util.FileUploadUtil;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileUploadServiceImpl implements S3FileUploadService {

  private final S3Presigner s3Presigner;

  private final S3Client s3Client;

  private final FileRecordRepository fileRecordRepository;

  private final KafkaProducerService kafkaProducerService;

  @Value("${aws.s3.bucket}")
  private String bucket;

  public CreateUploadResponse createUpload(CreateUploadRequest req) {
    log.info("Creating upload for file: {}", req.getFileName());

    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    String id = UUID.randomUUID().toString();
    String key = "uploads/" + id + "_" + req.getFileName();

    // Persist normalized model: metadata + lifecycle state
    FileRecord file = FileRecord.builder()
      .fileId(id)
      .fileName(req.getFileName())
      .contentType(req.getContentType())
      .sizeBytes(req.getSizeBytes())
      .s3Bucket(bucket)
      .s3Key(key)
      .status(FileUploadStatus.PENDING)
      .user(user)
      .build();

    fileRecordRepository.save(file);

    log.info("File metadata and upload state saved, fileId: {}", id);

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

    log.info("Presigned URL generated for upload, fileId: {}", id);

    CreateUploadResponse res = new CreateUploadResponse();
    res.setFileId(file.getFileId());
    res.setUploadUrl(preSigned.url().toString());
    res.setS3Key(key);
    return res;
  }

  public void completeUpload(String fileId, CompleteUploadRequest req) {
    log.info("Completing upload for fileId: {}", fileId);

    if (fileId == null || fileId.trim().isEmpty()) {
      throw new ClientException(ErrorCode.INVALID_INPUT, "File ID is required");
    }

    FileRecord file = fileRecordRepository.findById(fileId)
        .orElseThrow(() -> new ClientException(ErrorCode.RESOURCE_NOT_FOUND, "File not found for fileId: " + fileId));

    if (file.getStatus() == FileUploadStatus.UPLOADED) {
      log.warn("Upload already completed for fileId: {}", fileId);
      return; // already completed
    }

    if (file.getStatus() != FileUploadStatus.PENDING) {
      throw new ClientException(ErrorCode.CONFLICT, "Cannot complete upload in state: " + file.getStatus());
    }

    // 3️⃣ Verify object exists in S3
    HeadObjectResponse head;
    try {
      head = s3Client.headObject(
          HeadObjectRequest.builder()
            .bucket(file.getS3Bucket())
            .key(file.getS3Key())
              .build()
      );
    } catch (NoSuchKeyException e) {
      throw new ClientException(ErrorCode.RESOURCE_NOT_FOUND, "File not found in S3");
    } catch (S3Exception e) {

    if (e.statusCode() == 403) {
      // Access denied
      throw new ClientException(ErrorCode.FORBIDDEN, "You do not have permission to access this file");
    }

    throw new ServerException(ErrorCode.INTERNAL_ERROR, "S3 error occurred", e);
  }

    // 4️⃣ Size validation (CRITICAL)
    if (!Objects.equals(head.contentLength(), file.getSizeBytes())) {
      throw new ClientException(ErrorCode.CONFLICT, "Uploaded size mismatch. Expected=" + file.getSizeBytes() + ", actual=" + head.contentLength());
    }

    // 5️⃣ Optional checksum
    String s3Checksum = head.checksumSHA256();
    if (req != null && req.getChecksum() != null && !req.getChecksum().trim().isEmpty()) {
      if (s3Checksum == null || !req.getChecksum().equalsIgnoreCase(s3Checksum)) {
        throw new ClientException(ErrorCode.CONFLICT, "Checksum mismatch");
      }
      file.setChecksum(s3Checksum);
    } else {
      file.setChecksum(s3Checksum);
    }

    // 6️⃣ Finalize
    file.setStatus(FileUploadStatus.UPLOADED);
    file.setS3Url("s3://" + file.getS3Bucket() + "/" + file.getS3Key());

    fileRecordRepository.save(file);
    log.info("Upload completed and state updated for fileId: {}", fileId);

    FileUploadEvent fileUploadEvent = FileUploadUtil.getFileUploadEvent(file);
    kafkaProducerService.sendFileUploadEvent(fileUploadEvent);
    log.info("File upload event sent to Kafka for fileId: {}", fileId);
  }

}
