package com.aniket.mirror.upload.service;

import com.aniket.mirror.events.FileMirroredEvent;
import com.aniket.mirror.upload.dto.FileDetailsResponse;
import com.aniket.mirror.upload.dto.FileMirrorResponse;
import java.util.List;

public interface FileMirrorService {
    FileDetailsResponse getFileDetailsWithMirrors(String fileId);

    void updateMirrorStatus(FileMirroredEvent event);

    void reportMirrorFailure(String fileId, String providerName);
}
