package com.brickwork.users.controller.impl;

import com.brickwork.users.controller.AdminController;
import com.brickwork.users.dto.EmployeeRegistrationDTO;
import com.brickwork.users.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AdminControllerImpl implements AdminController {

    private final UserService userService;

    @Autowired
    AdminControllerImpl(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> registerNewAdmin(@RequestBody EmployeeRegistrationDTO request) {
        userService.registerNewAdmin(request);
        log.info("New admin account created: username={}", request.getUsername());
        return ResponseEntity.ok("New Administrator account created successfully.");
    }
}