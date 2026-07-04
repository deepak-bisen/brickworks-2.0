package com.brickwork.users.service.user;

import com.brickwork.users.enums.OtpVerificationStatus;

public interface OtpCacheService {

    String generateAndStoreOtp(String email);

    OtpVerificationStatus verifyOtp(String email, String otp);

    boolean isVerified(String email);

    void clear(String email);

}
