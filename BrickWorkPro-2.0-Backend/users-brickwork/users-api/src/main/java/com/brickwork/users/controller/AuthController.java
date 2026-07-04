package com.brickwork.users.controller;

import com.brickwork.users.dto.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
public interface AuthController {

    @PostMapping("/register")
    ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO);

    @PostMapping("/register/customer")
    ResponseEntity<?> registerCustomer(@RequestBody CustomerRegistrationDTO registrationDTO);

    @PostMapping("/register/employee")
    ResponseEntity<?> registerEmployee(@RequestBody EmployeeRegistrationDTO registrationDTO);

    @PostMapping("/login")
    ResponseEntity<JwtResponseDTO> authenticateUser(@RequestBody LoginRequestDTO loginRequest);

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam("username") String username);

    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestParam("username") String username, @RequestBody CustomerUpdateDTO updateDTO);

    @PostMapping("/forgot-password")
    ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request);
}