package com.aniket.mirror.upload.dto;

import lombok.Data;

@Data
public class CreateUploadResponse {
  private String fileId;
  private String uploadUrl;
  private String s3Key;
}
