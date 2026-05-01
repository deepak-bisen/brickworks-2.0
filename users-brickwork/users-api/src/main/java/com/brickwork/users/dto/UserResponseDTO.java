package com.brickwork.users.dto;

import com.brickwork.users.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private String id;
    private String username;
    private String email;
    private Role role;
    private String fullName;
    private String phoneNumber;
    private String companyName;
    private LocalDateTime createdAt;

}
