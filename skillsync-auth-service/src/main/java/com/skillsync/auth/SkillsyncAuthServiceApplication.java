package com.skillsync.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SkillsyncAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillsyncAuthServiceApplication.class, args);
	}

}
