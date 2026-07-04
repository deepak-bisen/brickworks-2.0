package com.brickwork.users.service.user.impl;

import com.brickwork.users.service.user.RedisOtpService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class RedisOtpServiceImpl implements RedisOtpService {

    private final StringRedisTemplate redisTemplate;

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    public RedisOtpServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String generateAndStoreOtp(String email) {

        SecureRandom secureRandom = new SecureRandom();

        String otp = String.valueOf(
                100000 + secureRandom.nextInt(900000)
        );

        redisTemplate.opsForValue().set(
                "otp:" + email,
                otp,
                Duration.ofMinutes(OTP_EXPIRY_MINUTES)
        );

        redisTemplate.opsForValue().set(
                "attempt:" + email,
                "0",
                Duration.ofMinutes(OTP_EXPIRY_MINUTES)
        );

        return otp;
    }

    @Override
    public boolean verifyOtp(String email, String otp) {

        String savedOtp =
                redisTemplate.opsForValue().get("otp:" + email);

        if (savedOtp == null)
            return false;

        String attempts =
                redisTemplate.opsForValue().get("attempt:" + email);

        int count = attempts == null ? 0 : Integer.parseInt(attempts);

        if (count >= MAX_ATTEMPTS) {
            clearOtp(email);
            return false;
        }

        if (!savedOtp.equals(otp)) {

            redisTemplate.opsForValue().set(
                    "attempt:" + email,
                    String.valueOf(count + 1),
                    Duration.ofMinutes(OTP_EXPIRY_MINUTES)
            );

            return false;
        }

        redisTemplate.opsForValue().set(
                "verified:" + email,
                "true",
                Duration.ofMinutes(OTP_EXPIRY_MINUTES)
        );

        return true;
    }

    @Override
    public boolean isOtpVerified(String email) {

        String verified =
                redisTemplate.opsForValue().get("verified:" + email);

        return "true".equals(verified);
    }

    @Override
    public void clearOtp(String email) {

        redisTemplate.delete("otp:" + email);
        redisTemplate.delete("attempt:" + email);
        redisTemplate.delete("verified:" + email);
    }
}