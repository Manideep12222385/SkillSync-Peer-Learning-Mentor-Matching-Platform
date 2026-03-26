package com.skillsync.review;

import org.springframework.cache.annotation.EnableCaching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableCaching
@SpringBootApplication
@EnableFeignClients
public class SkillsyncReviewServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillsyncReviewServiceApplication.class, args);
	}

}
