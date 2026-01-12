package com.aniket.mirror.upload.util;

import com.aniket.mirror.upload.dto.kafka.FileUploadEvent;
import com.aniket.mirror.upload.entity.FileMetadata;
import org.apache.tomcat.util.http.fileupload.FileUpload;

public class FileUploadUtil {


  public static FileUploadEvent getFileUploadEvent(FileMetadata meta) {
    return FileUploadEvent.builder()
        .fileId(meta.getId())
        .fileName(meta.getFileName())
        .contentType(meta.getContentType())
        .sizeBytes(meta.getSizeBytes())
        .s3Bucket(meta.getS3Bucket())
        .s3Key(meta.getS3Key())
        .s3Url(meta.getS3Url())
        .createdAt(meta.getCreatedAt())
        .checksum(meta.getChecksum())
        .build();
  }
}
