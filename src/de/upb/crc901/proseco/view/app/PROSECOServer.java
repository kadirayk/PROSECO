package de.upb.crc901.proseco.view.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * 
 * Spring Boot web application starter
 * 
 * 
 * @author kadirayk
 *
 */
@SpringBootApplication
public class PROSECOServer {
	
	int MAX_MB_UPLOAD = 10;
	
    @Bean
    @Order(0)
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipart = new CommonsMultipartResolver();
        multipart.setMaxUploadSize(MAX_MB_UPLOAD * 1024 * 1024);
        multipart.setMaxUploadSizePerFile(MAX_MB_UPLOAD * 1024 * 1024);
        return multipart;
    }
	
	public void launch(String... args) {
		SpringApplication.run(PROSECOServer.class, args);
	}

	public static void main(String[] args) {
		new PROSECOServer().launch();
	}
}

