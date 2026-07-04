package com.brickwork.users.service.user;

public interface EmailService {
    void sendOtp(String email, String otp);
}
