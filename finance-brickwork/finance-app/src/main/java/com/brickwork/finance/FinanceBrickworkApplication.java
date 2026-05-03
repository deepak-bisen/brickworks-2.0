package com.brickwork.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.brickwork"})
@ComponentScan(basePackages = {"com.brickwork"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.brickwork.finance.client")
public class FinanceBrickworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceBrickworkApplication.class, args);
	}

}
