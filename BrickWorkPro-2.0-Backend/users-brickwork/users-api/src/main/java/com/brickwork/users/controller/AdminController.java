package com.brickwork.users.controller;

import com.brickwork.users.dto.EmployeeRegistrationDTO;
import org.springframework.http.ResponseEntity;

public interface AdminController {
    /**
     * only Admin is allowed to touch this
     */
    ResponseEntity<String> registerNewAdmin(EmployeeRegistrationDTO request);
}
