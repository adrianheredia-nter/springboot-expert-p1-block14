package com.nter.billing.security;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class AuthUser extends User {

    private final Long id;

    public AuthUser(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }

    public List<String> getRoles() {
        return getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(Authorities.ROLE_PREFIX))
                .map(authority -> authority.substring(Authorities.ROLE_PREFIX.length()))
                .sorted()
                .toList();
    }
}
