package com.brickwork.users.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OtpData {

    private String otp;

    private LocalDateTime expiryTime;

    private int attempts;

    private boolean verified;

}