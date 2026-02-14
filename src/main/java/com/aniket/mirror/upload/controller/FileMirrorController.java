package com.aniket.mirror.upload.controller;

import com.aniket.mirror.upload.dto.FileDetailsResponse;
import com.aniket.mirror.upload.dto.FileMirrorResponse;
import com.aniket.mirror.upload.service.FileMirrorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("files")
@RequiredArgsConstructor
public class FileMirrorController {

    private final FileMirrorService fileMirrorService;

    @GetMapping("/{fileId}/mirrors")
    public ResponseEntity<FileDetailsResponse> getMirrors(@PathVariable String fileId) {
        log.info("Fetching mirrors for fileId: {}", fileId);
        return ResponseEntity.ok(fileMirrorService.getFileDetailsWithMirrors(fileId));
    }





    @PostMapping("/{fileId}/mirrors/{providerName}/report-failure")
    public ResponseEntity<Void> reportFailure(
            @PathVariable String fileId,
            @PathVariable String providerName) {
        log.info("Reporting failure for fileId: {}, provider: {}", fileId, providerName);
        fileMirrorService.reportMirrorFailure(fileId, providerName);
        return ResponseEntity.accepted().build();
    }
}
