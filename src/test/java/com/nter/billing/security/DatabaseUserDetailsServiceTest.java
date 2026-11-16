package com.nter.billing.security;

import com.nter.billing.entity.Role;
import com.nter.billing.entity.UserJPA;
import com.nter.billing.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.EnumSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DatabaseUserDetailsService userDetailsService;

    @Test
    void expiredPasswordAddsPasswordExpiredAndUserWithoutTouchingStoredRoles() {
        UserJPA stored = user(true);
        when(userRepository.findByUsername("expired.user")).thenReturn(Optional.of(stored));

        AuthUser result = userDetailsService.loadUserByUsername("expired.user");

        assertThat(result.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_READ_CONTRACT", "ROLE_USER", "PASSWORD_EXPIRED");
        assertThat(result.getId()).isEqualTo(6L);
        assertThat(stored.getRoles()).containsExactly(Role.READ_CONTRACT);
        verify(userRepository, never()).save(any());
    }

    @Test
    void activePasswordOnlyAddsUser() {
        when(userRepository.findByUsername("expired.user")).thenReturn(Optional.of(user(false)));

        AuthUser result = userDetailsService.loadUserByUsername("expired.user");

        assertThat(result.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_READ_CONTRACT", "ROLE_USER");
    }

    @Test
    void unknownUserFails() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private UserJPA user(boolean passwordExpired) {
        return UserJPA.builder()
                .id(6L)
                .username("expired.user")
                .password("hash")
                .passwordExpired(passwordExpired)
                .roles(EnumSet.of(Role.READ_CONTRACT))
                .build();
    }
}
