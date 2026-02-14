package com.aniket.mirror.upload.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "file_mirrors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMirror {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "file_id", nullable = false)
  private FileRecord file;

  @Column(name = "provider_name", nullable = false)
  private String providerName;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "external_url", length = 1000)
  private String externalUrl;

  @Column(name = "error_message", length = 1000)
  private String errorMessage;

  @Column(name = "mirrored_at")
  private Instant mirroredAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
