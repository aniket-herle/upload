package com.aniket.mirror.upload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateUploadRequest {

  @NotBlank
  private String fileName;

  @NotBlank
  private String contentType;

  @NotNull
  @Positive
  private Long sizeBytes;
}
