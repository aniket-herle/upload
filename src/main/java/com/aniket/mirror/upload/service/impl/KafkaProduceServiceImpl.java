package com.aniket.mirror.upload.service.impl;


import com.aniket.mirror.events.FileUploadEvent;
import com.aniket.mirror.upload.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProduceServiceImpl implements KafkaProducerService {

    private static final String TOPIC = "file_upload";


    private final KafkaTemplate<String, FileUploadEvent> kafkaTemplate;

    public void sendFileUploadEvent(FileUploadEvent fileUploadEvent) {
      log.info("Sending file upload event to Kafka for fileId: {}", fileUploadEvent.getFileId());
      kafkaTemplate.send(TOPIC, fileUploadEvent);
    }
}
