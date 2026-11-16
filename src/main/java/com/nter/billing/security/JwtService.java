package com.nter.billing.security;

import com.nter.billing.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String USER_ID_CLAIM = "userId";
    public static final String ROLES_CLAIM = "roles";

    private final JwtProperties properties;
    private final SecretKey key;
    private final Clock clock;

    public JwtService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.clock = clock;
    }

    public String generateToken(AuthUser user) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.expiration());
        return Jwts.builder()
                .header().type("JWT").and()
                .issuer(properties.issuer())
                .id(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .claim(USER_ID_CLAIM, user.getId())
                .claim(ROLES_CLAIM, user.getRoles())
                .issuedAt(Date.from(issuedAt))
                .notBefore(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.issuer())
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationSeconds() {
        return properties.expiration().toSeconds();
    }
}
