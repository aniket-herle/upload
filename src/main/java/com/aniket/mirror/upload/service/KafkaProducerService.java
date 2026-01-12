package com.aniket.mirror.upload.service;


import com.aniket.mirror.upload.dto.kafka.FileUploadEvent;

public interface KafkaProducerService {
  void sendFileUploadEvent(FileUploadEvent fileUploadEvent);
}
