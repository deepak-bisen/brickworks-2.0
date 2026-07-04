package com.brickwork.users.service.user.impl;

import com.brickwork.users.entity.OtpData;
import com.brickwork.users.enums.OtpVerificationStatus;
import com.brickwork.users.service.user.OtpCacheService;
import org.springframework.stereotype.Service;


import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpCacheServiceImpl implements OtpCacheService {

    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    private static final int EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    @Override
    public String generateAndStoreOtp(String email) {

        SecureRandom random = new SecureRandom();

        String otp = String.valueOf(
                100000 + random.nextInt(900000));

        otpStore.put(email,
                new OtpData(
                        otp,
                        LocalDateTime.now().plusMinutes(EXPIRY_MINUTES),
                        0,
                        false));

        return otp;
    }

    @Override
    public OtpVerificationStatus verifyOtp(String email, String otp) {

        OtpData data = otpStore.get(email);

        if (data == null)
            return OtpVerificationStatus.OTP_NOT_FOUND;

        if (LocalDateTime.now().isAfter(data.getExpiryTime())) {

            otpStore.remove(email);

            return OtpVerificationStatus.OTP_EXPIRED;
        }

        if (data.getAttempts() >= MAX_ATTEMPTS) {

            otpStore.remove(email);

            return OtpVerificationStatus.MAX_ATTEMPTS_EXCEEDED;
        }

        if (!data.getOtp().equals(otp)) {

            data.setAttempts(data.getAttempts() + 1);

            return OtpVerificationStatus.INVALID_OTP;
        }

        data.setVerified(true);

        return OtpVerificationStatus.VERIFIED;
    }

    @Override
    public boolean isVerified(String email) {

        OtpData data = otpStore.get(email);

        return data != null && data.isVerified();
    }

    @Override
    public void clear(String email) {

        otpStore.remove(email);
    }
}