package com.nter.billing.security;

import com.nter.billing.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "b3h7YWJ2N3Jwc2Q4ZjJrM2w1bTlxMXo0eDZ2OHQwdzJ5NGE2YzhlMGcyaTRrNm0=";
    private static final String ISSUER = "Exam_adrian_16/11/2026";
    private static final Instant NOW = Instant.parse("2026-11-16T10:00:00Z");

    private final JwtProperties properties = new JwtProperties(ISSUER, SECRET, Duration.ofHours(3));
    private final JwtService jwtService = new JwtService(properties, Clock.fixed(NOW, ZoneOffset.UTC));

    private final AuthUser user = new AuthUser(7L, "contract.reader", "hash", List.of(
            new SimpleGrantedAuthority("ROLE_READ_CONTRACT"),
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("PASSWORD_EXPIRED")));

    @Test
    void tokenContainsOnlyTheRequiredClaims() {
        String token = jwtService.generateToken(user);

        Claims claims = jwtService.parseToken(token);

        assertThat(claims.keySet()).containsExactlyInAnyOrder("iss", "jti", "sub", "userId", "roles", "iat", "nbf", "exp");
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getSubject()).isEqualTo("contract.reader");
        assertThat(claims.get("userId", Integer.class)).isEqualTo(7);
        assertThat(claims.get("roles", List.class)).containsExactly("READ_CONTRACT", "USER");
        assertThat(claims.getId()).isNotBlank();
    }

    @Test
    void tokenIsValidFromIssueTimeForThreeHours() {
        Claims claims = jwtService.parseToken(jwtService.generateToken(user));

        assertThat(claims.getIssuedAt().toInstant()).isEqualTo(NOW);
        assertThat(claims.getNotBefore().toInstant()).isEqualTo(NOW);
        assertThat(claims.getExpiration().toInstant()).isEqualTo(NOW.plus(Duration.ofHours(3)));
    }

    @Test
    void headerDeclaresJwtType() {
        String token = jwtService.generateToken(user);
        String header = new String(Decoders.BASE64URL.decode(token.substring(0, token.indexOf('.'))));

        assertThat(header).contains("\"typ\":\"JWT\"");
    }

    @Test
    void everyTokenGetsADifferentId() {
        String first = jwtService.parseToken(jwtService.generateToken(user)).getId();
        String second = jwtService.parseToken(jwtService.generateToken(user)).getId();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void expiredTokenIsRejected() {
        String token = jwtService.generateToken(user);
        Clock later = Clock.fixed(NOW.plus(Duration.ofHours(3)).plusSeconds(1), ZoneOffset.UTC);

        JwtService serviceLater = new JwtService(properties, later);

        assertThatThrownBy(() -> serviceLater.parseToken(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        String token = Jwts.builder()
                .issuer("someone_else")
                .subject("admin")
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        assertThatThrownBy(() -> jwtService.parseToken(token)).isInstanceOf(IncorrectClaimException.class);
    }
}
