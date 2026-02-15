package com.aniket.mirror.upload.service.impl;

import com.aniket.mirror.events.FileMirrorCheckEvent;
import com.aniket.mirror.events.FileMirroredEvent;
import com.aniket.mirror.upload.dto.FileDetailsResponse;
import com.aniket.mirror.upload.dto.FileMirrorResponse;
import com.aniket.mirror.upload.entity.FileMirror;
import com.aniket.mirror.upload.entity.FileRecord;
import com.aniket.mirror.upload.entity.User;
import com.aniket.mirror.upload.exception.ClientException;
import com.aniket.mirror.upload.exception.ErrorCode;
import com.aniket.mirror.upload.repository.FileMirrorRepository;
import com.aniket.mirror.upload.repository.FileRecordRepository;
import com.aniket.mirror.upload.service.FileMirrorService;
import com.aniket.mirror.upload.service.KafkaProducerService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileMirrorServiceImpl implements FileMirrorService {

    private final FileMirrorRepository fileMirrorRepository;

    private final FileRecordRepository fileRecordRepository;

    private final KafkaProducerService kafkaProducerService;

    @Override
    public FileDetailsResponse getFileDetailsWithMirrors(String fileId) {
        log.info("Fetching file details and mirrors for fileId: {}", fileId);
        
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        FileRecord file = fileRecordRepository.findById(fileId)
            .orElseThrow(() -> new ClientException(ErrorCode.RESOURCE_NOT_FOUND, "File not found with id: " + fileId));

        if (file.getUser() != null && !file.getUser().getId().equals(user.getId())) {
             throw new ClientException(ErrorCode.FORBIDDEN, "You are not authorized to access this file");
        }

        List<FileMirrorResponse> mirrors = fileMirrorRepository.findByFile_FileId(fileId).stream()
            .map(m -> FileMirrorResponse.builder()
                .providerName(m.getProviderName())
                .status(m.getStatus())
                .externalUrl(m.getExternalUrl())
                .errorMessage(m.getErrorMessage())
                .mirroredAt(m.getMirroredAt())
                .build())
            .collect(Collectors.toList());

        return FileDetailsResponse.builder()
            .fileId(file.getFileId())
            .fileName(file.getFileName())
            .contentType(file.getContentType())
            .sizeBytes(file.getSizeBytes())
            .status(file.getStatus())
            .checksum(file.getChecksum())
            .createdAt(file.getCreatedAt())
            .updatedAt(file.getUpdatedAt())
            .mirrors(mirrors)
            .build();
    }

    @Override
    @Transactional
    public void updateMirrorStatus(FileMirroredEvent event) {
        log.info("Updating mirror status for fileId: {}, provider: {}", event.getFileId(), event.getProviderName());

        FileRecord file = fileRecordRepository.findById(event.getFileId())
            .orElse(null);

        if (file == null) {
            log.error("File record not found for fileId: {}", event.getFileId());
            return;
        }

        FileMirror mirror = fileMirrorRepository
            .findByFile_FileIdAndProviderName(event.getFileId(), event.getProviderName())
            .orElseGet(() -> FileMirror.builder()
                .file(file)
                .providerName(event.getProviderName())
                .build());

        mirror.setStatus(event.getStatus());
        mirror.setExternalUrl(event.getExternalUrl());
        mirror.setErrorMessage(event.getErrorMessage());
        mirror.setMirroredAt(event.getMirroredAt());

        fileMirrorRepository.save(mirror);
    }

    @Override
    public void reportMirrorFailure(String fileId, String providerName) {
        log.info("Reporting mirror failure for fileId: {}, provider: {}", fileId, providerName);
        // Optional: Update status to 'CHECKING' in DB
        kafkaProducerService.sendFileMirrorCheckEvent(new FileMirrorCheckEvent(fileId, providerName));
    }
}
