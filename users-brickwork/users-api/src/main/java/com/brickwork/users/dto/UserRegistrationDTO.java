package com.brickwork.users.dto;

import com.brickwork.users.enums.Role;
import lombok.Data;

@Data
public class UserRegistrationDTO {
    private String username;
    private String email;
    private String password;
    private Role role; // ADMIN, CONTRACTOR, or STAFF
    private String fullName;
    private String phoneNumber;
    private String companyName;
}
