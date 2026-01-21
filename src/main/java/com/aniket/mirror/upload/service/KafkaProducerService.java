package com.aniket.mirror.upload.service;


import com.aniket.mirror.events.FileUploadEvent;

public interface KafkaProducerService {
  void sendFileUploadEvent(FileUploadEvent fileUploadEvent);
}
