package com.steve.secure_auth;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;



@EnableCaching
@SpringBootApplication
public class SecureAuthApplication {

	public static void main(String[] args) {
		// Load .env FIRST before Spring starts
		Dotenv dotenv = Dotenv.configure()
				.directory("./")  // project root
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});

		SpringApplication.run(SecureAuthApplication.class, args);
	}
}


