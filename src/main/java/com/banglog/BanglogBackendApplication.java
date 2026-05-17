package com.banglog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BanglogBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BanglogBackendApplication.class, args);
	}

}
