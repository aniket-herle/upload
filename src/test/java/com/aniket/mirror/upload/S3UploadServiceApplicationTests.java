package com.aniket.mirror.upload;

import com.aniket.mirror.upload.controller.S3UploadController;
import com.aniket.mirror.upload.service.S3FileUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(controllers = S3UploadController.class)
class S3UploadServiceApplicationTests {

	@MockBean
	private S3FileUploadService s3FileUploadService;

	@Test
	void contextLoads() {
	}

}
