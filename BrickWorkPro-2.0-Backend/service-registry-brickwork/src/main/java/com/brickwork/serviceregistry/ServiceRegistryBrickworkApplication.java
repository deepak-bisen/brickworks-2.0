package com.brickwork.serviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class ServiceRegistryBrickworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryBrickworkApplication.class, args);
    }

}
