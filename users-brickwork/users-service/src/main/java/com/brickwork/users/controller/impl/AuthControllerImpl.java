package com.brickwork.users.controller.impl;

import com.brickwork.security.util.JwtUtil;
import com.brickwork.users.controller.AuthController;
import com.brickwork.users.dto.CustomerRegistrationDTO;
import com.brickwork.users.dto.EmployeeRegistrationDTO;
import com.brickwork.users.dto.JwtResponseDTO;
import com.brickwork.users.dto.LoginRequestDTO;
import com.brickwork.users.dto.UserDTO;
import com.brickwork.users.entity.User;
import com.brickwork.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

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
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            User user = userService.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Call the new overloaded method from the common library!
            String jwt = jwtUtil.generateCustomToken(
                    user.getUsername(),
                    user.getRole().name(),
                    user.getUserId()
            );
            return ResponseEntity.ok(new JwtResponseDTO(
                    jwt,
                    user.getUsername(),
                    user.getRole().name()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
}