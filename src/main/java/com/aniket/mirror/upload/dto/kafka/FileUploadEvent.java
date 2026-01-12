package com.aniket.mirror.upload.dto.kafka;

import jakarta.persistence.Column;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Builder
public class FileUploadEvent {
  private String fileId;

  private String fileName;

  private String contentType;

  private Long sizeBytes;

  @Column(nullable = false)
  private String s3Bucket;

  @Column(nullable = false)
  private String s3Key;

  @Column(columnDefinition = "TEXT")
  private String s3Url;

  private String checksum;

  private Instant createdAt;
}
