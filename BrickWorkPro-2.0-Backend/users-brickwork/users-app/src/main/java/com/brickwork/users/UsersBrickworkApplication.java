package com.brickwork.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@ComponentScan(basePackages = "com.brickwork")
@RestController // <--- TEMPORARY ANNOTATION FOR HEALTH CHECK
public class UsersBrickworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsersBrickworkApplication.class, args);
    }

    // <--- TEMPORARY PING METHOD FOR HEALTH CHECK--->
    @GetMapping("/ping")
    public String ping() {
        return "Users Service is UP and responding!";
    }
}