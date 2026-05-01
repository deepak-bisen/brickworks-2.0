package com.brickwork.users.controller;

import com.brickwork.users.dto.LoginRequestDTO;
import com.brickwork.users.dto.UserRegistrationDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/auth")
public interface AuthController {

    @PostMapping("/register")
    ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO registrationDTO);

    @PostMapping("/login")
    ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDTO loginRequest);

}
