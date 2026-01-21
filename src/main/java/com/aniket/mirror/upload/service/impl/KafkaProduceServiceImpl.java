package com.aniket.mirror.upload.service.impl;


import com.aniket.mirror.events.FileUploadEvent;
import com.aniket.mirror.upload.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProduceServiceImpl implements KafkaProducerService {

    private static final String TOPIC = "file_upload";


    private final KafkaTemplate<String, FileUploadEvent> kafkaTemplate;

    public void sendFileUploadEvent(FileUploadEvent fileUploadEvent) {
      kafkaTemplate.send(TOPIC, fileUploadEvent);
    }
}
