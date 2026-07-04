package com.brickwork.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequestDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String otp;

}