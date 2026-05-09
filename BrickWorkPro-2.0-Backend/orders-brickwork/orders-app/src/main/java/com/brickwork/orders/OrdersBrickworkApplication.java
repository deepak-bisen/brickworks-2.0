package com.brickwork.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.brickwork"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.brickwork.orders.client")
@ComponentScan(basePackages = {"com.brickwork"})
public class OrdersBrickworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersBrickworkApplication.class, args);
    }

}
