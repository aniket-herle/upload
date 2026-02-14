package com.aniket.mirror.upload.consumer;

import com.aniket.mirror.events.FileMirroredEvent;
import com.aniket.mirror.upload.service.FileMirrorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileMirroredConsumer {

  private final FileMirrorService fileMirrorService;

  @KafkaListener(
      topics = "${mirror.kafka.file-mirrored-topic:file_mirrored}",
      groupId = "${spring.kafka.consumer.group-id:file-mirror-status-group}"
  )
  public void consume(FileMirroredEvent event) {
    log.info("Received FileMirroredEvent: {}", event);
    fileMirrorService.updateMirrorStatus(event);
  }
}
