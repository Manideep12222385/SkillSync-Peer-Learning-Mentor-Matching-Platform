package com.skillsync.user;

import org.springframework.cache.annotation.EnableCaching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableCaching
@SpringBootApplication
@EnableFeignClients
public class SkillsyncUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillsyncUserServiceApplication.class, args);
	}

}
