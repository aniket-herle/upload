package com.aniket.mirror.upload.util;

import com.aniket.mirror.events.FileUploadEvent;
import com.aniket.mirror.upload.entity.FileRecord;
import java.util.UUID;

public class FileUploadUtil {


  public static FileUploadEvent getFileUploadEvent(FileRecord file) {
    return new FileUploadEvent(
        UUID.randomUUID().toString(),     // eventId
        file.getFileId(),       // fileId
        file.getFileName(),     // fileName
        file.getContentType(),  // contentType
        file.getSizeBytes(),    // sizeBytes (long)
        file.getS3Bucket(),     // s3Bucket
        file.getS3Key(),        // s3Key
        file.getS3Url(),        // s3Url
        file.getChecksum(),     // checksum
        file.getCreatedAt(),    // createdAt (Instant)
        1   // version (int)
    );

  }
}
