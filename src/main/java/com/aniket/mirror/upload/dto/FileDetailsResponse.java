package com.aniket.mirror.upload.dto;

import com.aniket.mirror.upload.constants.enums.FileUploadStatus;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDetailsResponse {
    private String fileId;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
    private FileUploadStatus status;
    private String checksum;
    private Instant createdAt;
    private Instant updatedAt;
    private List<FileMirrorResponse> mirrors;
}
