package com.brickwork.users.controller.impl;

import com.brickwork.users.controller.AdminController;
import com.brickwork.users.dto.EmployeeRegistrationDTO;
import com.brickwork.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminControllerImpl implements AdminController {

    /**
     * only admin has access
     * @return new admin
     */
    @Autowired
    private UserService userService;

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')") // CRITICAL: Only an existing Admin can call this!
    public ResponseEntity<String> registerNewAdmin(@RequestBody EmployeeRegistrationDTO request) {
        try {
            // Pass the DTO to the service layer for processing
            userService.registerNewAdmin(request);

            return ResponseEntity.ok("New Administrator account created successfully.");

        } catch (IllegalArgumentException e) {
            // Catch validation errors (e.g., username already taken)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            // Catch database or server errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create admin: " + e.getMessage());
        }
    }
}
