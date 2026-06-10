package com.brickwork.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import com.brickwork.finance.config.FeignConfig;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.brickwork"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.brickwork.finance.client", defaultConfiguration = FeignConfig.class)
public class FinanceBrickworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceBrickworkApplication.class, args);
	}

}
