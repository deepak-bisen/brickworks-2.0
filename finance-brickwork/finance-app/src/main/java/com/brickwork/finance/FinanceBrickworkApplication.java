package com.brickwork.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.brickwork"})
@EnableDiscoveryClient
@EnableFeignClients
public class FinanceBrickworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceBrickworkApplication.class, args);
	}

}
