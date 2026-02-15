package com.aniket.mirror.upload.entity;

import com.aniket.mirror.upload.constants.enums.FileUploadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "files",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_files_s3", columnNames = {"s3_bucket", "s3_key"})
    },
    indexes = {
        @Index(name = "idx_files_created_at", columnList = "created_at"),
        @Index(name = "idx_files_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileRecord {

  @Id
  @Column(name = "file_id", nullable = false, updatable = false)
  private String fileId;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "size_bytes", nullable = false)
  private Long sizeBytes;

  @Column(name = "s3_bucket", nullable = false)
  private String s3Bucket;

  @Column(name = "s3_key", nullable = false)
  private String s3Key;

  @Column(name = "s3_url", columnDefinition = "TEXT")
  private String s3Url;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private FileUploadStatus status;

  @Column(name = "checksum")
  private String checksum;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
