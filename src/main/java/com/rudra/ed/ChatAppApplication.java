package com.rudra.ed;

import com.rudra.ed.config.DotEnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatAppApplication {

	public static void main(String[] args) {
		DotEnvLoader.load();
		SpringApplication.run(ChatAppApplication.class, args);
	}
}
