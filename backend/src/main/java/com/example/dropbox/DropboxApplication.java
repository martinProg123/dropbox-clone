package com.example.dropbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DropboxApplication {

	public static void main(String[] args) {
		SpringApplication.run(DropboxApplication.class, args);
	}

}
