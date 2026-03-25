package com.skillsync.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SkillsyncApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillsyncApiGatewayApplication.class, args);
	}

}
