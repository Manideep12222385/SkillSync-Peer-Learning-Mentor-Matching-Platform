package com.skillsync.mentor;

import org.springframework.cache.annotation.EnableCaching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableCaching
@SpringBootApplication
public class SkillsyncMentorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillsyncMentorServiceApplication.class, args);
	}

}
