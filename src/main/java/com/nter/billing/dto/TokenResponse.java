package com.nter.billing.dto;

public record TokenResponse(String token, String tokenType, long expiresIn) {

    public static TokenResponse bearer(String token, long expiresIn) {
        return new TokenResponse(token, "Bearer", expiresIn);
    }
}
