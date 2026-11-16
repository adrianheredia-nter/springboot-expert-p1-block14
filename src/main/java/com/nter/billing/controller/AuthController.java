package com.nter.billing.controller;

import com.nter.billing.constants.AppConstants;
import com.nter.billing.dto.ChangePasswordRequest;
import com.nter.billing.dto.LoginRequest;
import com.nter.billing.dto.TokenResponse;
import com.nter.billing.security.AuthUser;
import com.nter.billing.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(AppConstants.LOGIN_PATH)
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping(AppConstants.REFRESH_PATH)
    public TokenResponse refresh(@AuthenticationPrincipal AuthUser user) {
        return authService.refresh(user);
    }

    @PostMapping(AppConstants.CHANGE_PASSWORD_PATH)
    @PreAuthorize("hasAuthority('PASSWORD_EXPIRED') or hasRole('ADMIN')")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AuthUser user,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(user, request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
