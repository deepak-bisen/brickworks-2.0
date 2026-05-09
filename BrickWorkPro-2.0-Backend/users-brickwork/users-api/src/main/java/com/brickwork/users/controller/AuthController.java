package com.brickwork.users.controller;

import com.brickwork.users.dto.CustomerRegistrationDTO;
import com.brickwork.users.dto.EmployeeRegistrationDTO;
import com.brickwork.users.dto.LoginRequestDTO;
import com.brickwork.users.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/auth")
public interface AuthController {

    @PostMapping("/register")
    ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO);

    @PostMapping("/register/customer")
    ResponseEntity<?> registerCustomer(@RequestBody CustomerRegistrationDTO registrationDTO);

    @PostMapping("/register/employee")
    ResponseEntity<?> registerEmployee(@RequestBody EmployeeRegistrationDTO registrationDTO);

    @PostMapping("/login")
    ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDTO loginRequest);
}