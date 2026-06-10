package com.brickwork.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import com.brickwork.orders.config.FeignConfig;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.brickwork"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.brickwork.orders.client", defaultConfiguration = FeignConfig.class)
@ComponentScan(basePackages = {"com.brickwork"})
public class
OrdersBrickworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersBrickworkApplication.class, args);
    }

}
