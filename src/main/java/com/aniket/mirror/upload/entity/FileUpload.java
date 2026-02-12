package com.aniket.mirror.upload.entity;

import com.aniket.mirror.upload.constants.enums.FileUploadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "file_uploads",
    indexes = {
        @Index(name = "idx_upload_status", columnList = "status"),
        @Index(name = "idx_upload_updated_at", columnList = "updated_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUpload {

  @Id
  @Column(name = "file_id", nullable = false, updatable = false)
  private String fileId;

  @OneToOne(optional = false)
  @MapsId
  @JoinColumn(name = "file_id", nullable = false)
  private FileRecord file;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private FileUploadStatus status;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
