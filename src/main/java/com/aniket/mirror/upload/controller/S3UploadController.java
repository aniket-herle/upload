package com.aniket.mirror.upload.controller;

import com.aniket.mirror.upload.dto.CompleteUploadRequest;
import com.aniket.mirror.upload.dto.CreateUploadRequest;
import com.aniket.mirror.upload.dto.CreateUploadResponse;
import com.aniket.mirror.upload.service.S3FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("upload")
@RequiredArgsConstructor
public class S3UploadController {

    private final S3FileUploadService s3FileUploadService;


    @PostMapping("/init-upload")
    CreateUploadResponse initUpload(@RequestBody CreateUploadRequest req) {
        log.info("Initializing upload for file: {}", req.getFileName());
        return s3FileUploadService.createUpload(req);
    }

    @PostMapping("/{fileId}/complete-upload")
    public ResponseEntity<Void> completeUpload(
        @PathVariable String fileId,
        @RequestBody(required = false) CompleteUploadRequest req) {
        log.info("Completing upload for fileId: {}", fileId);
        s3FileUploadService.completeUpload(fileId, req);
        return ResponseEntity.noContent().build();
    }

}
