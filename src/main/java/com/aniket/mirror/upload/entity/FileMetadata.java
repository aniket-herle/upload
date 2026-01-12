package com.aniket.mirror.upload.entity;

import com.aniket.mirror.upload.constants.enums.FileUploadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {

  @Id
  private String id; // UUID from backend

  @Column(nullable = false)
  private String fileName;

  private String contentType;

  @Column(nullable = false)
  private Long sizeBytes;

  @Column(nullable = false)
  private String s3Bucket;

  @Column(nullable = false)
  private String s3Key;

  @Column(columnDefinition = "TEXT")
  private String s3Url;          // nullable

  private String checksum;       // nullable

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FileUploadStatus status;

  @CreationTimestamp
  private Instant createdAt;

  @UpdateTimestamp
  private Instant updatedAt;
}
