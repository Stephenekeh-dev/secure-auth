package com.steve.secure_auth;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SecureAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecureAuthApplication.class, args);
	}

}
