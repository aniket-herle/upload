package com.aniket.mirror.upload.service.impl;


import com.aniket.mirror.events.FileMirrorCheckEvent;
import com.aniket.mirror.events.FileUploadEvent;
import com.aniket.mirror.upload.config.properties.MirrorKafkaProperties;
import com.aniket.mirror.upload.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProduceServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

  private final MirrorKafkaProperties kafkaProperties;

    public void sendFileUploadEvent(FileUploadEvent fileUploadEvent) {
      log.info("Sending file upload event to Kafka for fileId: {}", fileUploadEvent.getFileId());
      String topic = kafkaProperties.getFileUploadTopic();
      if (topic == null || topic.isBlank()) {
        throw new IllegalStateException("mirror.kafka.file-upload-topic is required");
      }
      ProducerRecord<String, Object> record =
          new ProducerRecord<>(topic, fileUploadEvent.getFileId(), fileUploadEvent);

      String traceId = MDC.get("traceId");
      if (traceId != null && !traceId.isBlank()) {
        record.headers().add("X-Trace-Id", traceId.getBytes(StandardCharsets.UTF_8));
      }
      if (fileUploadEvent.getEventId() != null && !fileUploadEvent.getEventId().isBlank()) {
        record.headers().add("X-Event-Id", fileUploadEvent.getEventId().getBytes(StandardCharsets.UTF_8));
      }

      kafkaTemplate.send(record);
    }

    @Override
    public void sendFileMirrorCheckEvent(FileMirrorCheckEvent event) {
        log.info("Sending file mirror check event to Kafka for fileId: {}, provider: {}", event.getFileId(), event.getProviderName());
        String topic = kafkaProperties.getFileMirrorCheckTopic();
        if (topic == null || topic.isBlank()) {
            throw new IllegalStateException("mirror.kafka.file-mirror-check-topic is required");
        }
        kafkaTemplate.send(topic, event.getFileId(), event);
    }
}
