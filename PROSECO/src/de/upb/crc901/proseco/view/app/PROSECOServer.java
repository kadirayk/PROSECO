package de.upb.crc901.proseco.view.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
	
	public void launch(String... args) {
		SpringApplication.run(PROSECOServer.class, args);
	}

	public static void main(String[] args) {
		new PROSECOServer().launch();
	}

}
