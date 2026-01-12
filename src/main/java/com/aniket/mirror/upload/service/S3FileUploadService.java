package com.aniket.mirror.upload.service;

import com.aniket.mirror.upload.dto.CompleteUploadRequest;
import com.aniket.mirror.upload.dto.CreateUploadRequest;
import com.aniket.mirror.upload.dto.CreateUploadResponse;

public interface S3FileUploadService {
  CreateUploadResponse createUpload(CreateUploadRequest req);

  void completeUpload(String fileId, CompleteUploadRequest req);
}
