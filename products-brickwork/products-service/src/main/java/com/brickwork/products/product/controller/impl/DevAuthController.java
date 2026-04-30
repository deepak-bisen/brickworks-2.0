package com.brickwork.products.product.controller.impl;

import com.brickwork.products.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/dev")
public class DevAuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/token")
    public ResponseEntity<String> getTestToken() {
        UserDetails dummyUser = new User("admin_test_user", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(dummyUser);
        return ResponseEntity.ok(token);
    }
}