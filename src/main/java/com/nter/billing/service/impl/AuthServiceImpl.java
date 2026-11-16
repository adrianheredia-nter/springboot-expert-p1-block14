package com.nter.billing.service.impl;

import com.nter.billing.constants.AppConstants;
import com.nter.billing.dto.LoginRequest;
import com.nter.billing.dto.TokenResponse;
import com.nter.billing.entity.UserJPA;
import com.nter.billing.exception.InvalidDataException;
import com.nter.billing.exception.NotFoundException;
import com.nter.billing.repository.UserRepository;
import com.nter.billing.security.AuthUser;
import com.nter.billing.security.JwtService;
import com.nter.billing.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return issueToken((AuthUser) authentication.getPrincipal());
    }

    @Override
    public TokenResponse refresh(AuthUser user) {
        return issueToken(user);
    }

    @Override
    @Transactional
    public void changePassword(AuthUser authUser, String newPassword) {
        UserJPA user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new NotFoundException("User " + authUser.getUsername() + " not found"));
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new InvalidDataException(AppConstants.SAME_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordExpired(false);
    }

    private TokenResponse issueToken(AuthUser user) {
        return TokenResponse.bearer(jwtService.generateToken(user), jwtService.getExpirationSeconds());
    }
}
