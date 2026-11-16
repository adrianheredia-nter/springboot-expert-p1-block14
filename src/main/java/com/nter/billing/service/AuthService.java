package com.nter.billing.service;

import com.nter.billing.dto.LoginRequest;
import com.nter.billing.dto.TokenResponse;
import com.nter.billing.security.AuthUser;

public interface AuthService {

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(AuthUser user);

    void changePassword(AuthUser user, String newPassword);
}
