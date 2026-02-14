package com.aniket.mirror.upload.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "mirror.kafka")
public class MirrorKafkaProperties {

  /** Topic name for emitting FileUploadEvent messages. */
  private String fileUploadTopic;

  private String fileMirrorCheckTopic;

}
