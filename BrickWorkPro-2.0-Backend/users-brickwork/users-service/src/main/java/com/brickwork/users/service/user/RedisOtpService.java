package com.brickwork.users.service.user;

public interface RedisOtpService {

    String generateAndStoreOtp(String email);

    boolean verifyOtp(String email, String otp);

    boolean isOtpVerified(String email);

    void clearOtp(String email);
}