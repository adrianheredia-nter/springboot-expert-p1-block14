package com.nter.billing.security;

import com.nter.billing.entity.UserJPA;
import com.nter.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public AuthUser loadUserByUsername(String username) {
        UserJPA user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
        return new AuthUser(user.getId(), user.getUsername(), user.getPassword(), buildAuthorities(user));
    }

    // USER and PASSWORD_EXPIRED are computed on every authentication and never stored
    private List<GrantedAuthority> buildAuthorities(UserJPA user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(Authorities.ROLE_PREFIX + role.name())));
        authorities.add(new SimpleGrantedAuthority(Authorities.ROLE_PREFIX + Authorities.USER));
        if (user.isPasswordExpired()) {
            authorities.add(new SimpleGrantedAuthority(Authorities.PASSWORD_EXPIRED));
        }
        return authorities;
    }
}
