package com.aniket.mirror.upload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class S3UploadServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(S3UploadServiceApplication.class, args);
	}

}
