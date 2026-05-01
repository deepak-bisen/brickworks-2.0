package com.brickwork.users.controller.impl;

import com.brickwork.users.controller.AuthController;
import com.brickwork.users.dto.JwtResponseDTO;
import com.brickwork.users.dto.LoginRequestDTO;
import com.brickwork.users.dto.UserDTO;
import com.brickwork.users.dto.UserRegistrationDTO;
import com.brickwork.users.entity.User;
import com.brickwork.users.security.JwtUtil;
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
    public ResponseEntity<?> registerUser(UserRegistrationDTO registrationDTO) {
      // 1. Validation Checks
      if (userService.existsByEmail(registrationDTO.getEmail())) {
          return ResponseEntity.badRequest().body("Error: Email already exists!");
      }
      if (userService.existsByUsername(registrationDTO.getUsername())) {
          return ResponseEntity.badRequest().body("Error: Username already exists!");
      }

      //2. Delegate to Service Layer
        UserDTO savedUser = userService.registerUser(registrationDTO);

      // Return the DTO in the 201 Created response
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Override
    public ResponseEntity<?> authenticateUser(LoginRequestDTO loginRequest) {
      try{
          //1. Authentication the user
          Authentication authentication = authenticationManager.authenticate(
                  new UsernamePasswordAuthenticationToken(
                          loginRequest.getUsername(),
                          loginRequest.getPassword()
                  )
          );

          //2. Load the user details and generate token
          User  user = userService.findByUsername(loginRequest.getUsername())
                  .orElseThrow(() -> new RuntimeException("User not found"));

          String jwt = jwtUtil.generateToken(user);

          return ResponseEntity.ok(new JwtResponseDTO(
                  jwt,
                  user.getUsername(),
                  user.getRole().name()
          ));
      } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Invalid username or password!");
      }

    }
}
