package com.aniket.mirror.upload.util;

import com.aniket.mirror.events.FileUploadEvent;
import com.aniket.mirror.upload.entity.FileMetadata;
import java.util.UUID;

public class FileUploadUtil {


  public static FileUploadEvent getFileUploadEvent(FileMetadata meta) {
    return new FileUploadEvent(
        UUID.randomUUID().toString(),     // eventId
        meta.getId(),           // fileId
        meta.getFileName(),     // fileName
        meta.getContentType(),  // contentType
        meta.getSizeBytes(),    // sizeBytes (long)
        meta.getS3Bucket(),     // s3Bucket
        meta.getS3Key(),        // s3Key
        meta.getS3Url(),        // s3Url
        meta.getChecksum(),     // checksum
        meta.getCreatedAt(),    // createdAt (Instant)
        1   // version (int)
    );

  }
}
