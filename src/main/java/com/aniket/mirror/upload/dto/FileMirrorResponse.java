package com.aniket.mirror.upload.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMirrorResponse {
  private String providerName;
  private String status;
  private String externalUrl;
  private String errorMessage;
  private Instant mirroredAt;
}
