package com.brickwork.users.enums;

public enum OtpVerificationStatus {
    VERIFIED,
    INVALID_OTP,
    OTP_EXPIRED,
    MAX_ATTEMPTS_EXCEEDED,
    OTP_NOT_FOUND
}
