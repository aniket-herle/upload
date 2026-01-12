package com.aniket.mirror.upload.dto;

import lombok.Data;

@Data
public class CompleteUploadRequest {
  private String checksum; // optional
}