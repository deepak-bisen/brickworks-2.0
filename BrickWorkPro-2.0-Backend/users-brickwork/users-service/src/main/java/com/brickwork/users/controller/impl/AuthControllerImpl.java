package com.brickwork.users.controller.impl;

import com.brickwork.exception.NotFoundException;
import com.brickwork.security.util.JwtUtil;
import com.brickwork.users.controller.AuthController;
import com.brickwork.users.dto.*;
import com.brickwork.users.entity.User;
import com.brickwork.users.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AuthControllerImpl implements AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public ResponseEntity<?> registerUser(UserDTO userDTO) {
        if (userService.existsByUsername(userDTO.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        if (userService.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        UserDTO savedUser = userService.registerUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Override
    public ResponseEntity<?> registerCustomer(CustomerRegistrationDTO registrationDTO) {
        if (userService.existsByUsername(registrationDTO.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        if (userService.existsByEmail(registrationDTO.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        // Custom Validation: If it's a BUSINESS, they must provide a Company Name
        if (registrationDTO.getCustomerType() == com.brickwork.users.enums.CustomerType.BUSINESS) {
            if (registrationDTO.getCompanyName() == null || registrationDTO.getCompanyName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Business customers must provide a Company Name.");
            }
        }

        UserDTO savedUser = userService.registerCustomer(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Override
    public ResponseEntity<?> registerEmployee(EmployeeRegistrationDTO registrationDTO) {
        if (userService.existsByUsername(registrationDTO.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        if (userService.existsByEmail(registrationDTO.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        UserDTO savedUser = userService.registerEmployee(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Override
    public ResponseEntity<?> authenticateUser(LoginRequestDTO loginRequest) {
        String username = loginRequest.getUsername() == null
                ? null
                : loginRequest.getUsername().trim();
        String password = loginRequest.getPassword();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String jwt = jwtUtil.generateCustomToken(
                user.getUsername(),
                user.getRole().name(),
                user.getId()
        );
        log.info("User authenticated successfully: username={}, role={}", user.getUsername(), user.getRole());
        return ResponseEntity.ok(new JwtResponseDTO(
                jwt,
                user.getUsername(),
                user.getRole().name()
        ));
    }

    @Override
    public ResponseEntity<?> getProfile(String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @Override
    public ResponseEntity<?> updateProfile(String username, CustomerUpdateDTO updateDTO) {
        return ResponseEntity.ok(userService.updateCustomerProfile(username, updateDTO));
    }

}