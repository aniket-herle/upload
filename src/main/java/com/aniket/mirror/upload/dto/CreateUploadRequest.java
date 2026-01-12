package com.aniket.mirror.upload.dto;

import lombok.Data;

@Data
public class CreateUploadRequest {

  private String fileName;

  private String contentType;

  private Long sizeBytes;
}
