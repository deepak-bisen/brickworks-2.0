package com.brickwork.users.controller;

import com.brickwork.users.dto.EmployeeRegistrationDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/auth")
public interface AdminController {
    /**
     * only Admin is allowed to touch this
     */
    ResponseEntity<String> registerNewAdmin(EmployeeRegistrationDTO request);
}
