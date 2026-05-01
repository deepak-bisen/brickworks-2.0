package com.brickwork.users.dto;

import com.brickwork.users.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    // Used for responses
    private String id;

    // Used for both requests and responses
    private String username;
    private String email;
    private Role role;
    private String fullName;
    private String phoneNumber;
    private String companyName;

    // Used ONLY for registration requests.
    // It will be ignored (left null) when returning responses to hide it.
    private String password;

    // Used for responses
    private LocalDateTime createdAt;
}
