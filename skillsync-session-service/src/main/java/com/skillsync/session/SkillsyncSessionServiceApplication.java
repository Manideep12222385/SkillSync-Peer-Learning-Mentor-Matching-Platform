package com.skillsync.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SkillsyncSessionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillsyncSessionServiceApplication.class, args);
	}

}
