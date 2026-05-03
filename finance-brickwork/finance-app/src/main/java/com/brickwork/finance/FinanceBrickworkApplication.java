package com.brickwork.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.brickwork"})
@EnableDiscoveryClient
public class FinanceBrickworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceBrickworkApplication.class, args);
	}

}
