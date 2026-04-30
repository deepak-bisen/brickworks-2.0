package com.brickwork.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.brickwork.orders.client")
public class OrdersBrickworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersBrickworkApplication.class, args);
    }

}
