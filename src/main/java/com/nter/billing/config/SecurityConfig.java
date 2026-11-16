package com.nter.billing.config;

import com.nter.billing.constants.AppConstants;
import com.nter.billing.entity.Role;
import com.nter.billing.security.DatabaseUserDetailsService;
import com.nter.billing.security.JwtAuthenticationFilter;
import com.nter.billing.security.JwtService;
import com.nter.billing.security.SecurityErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ADMIN = Role.ADMIN.name();

    private final JwtService jwtService;
    private final DatabaseUserDetailsService userDetailsService;
    private final SecurityErrorHandler securityErrorHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String contracts = AppConstants.CONTRACTS_PATH + "/**";
        String invoices = AppConstants.INVOICES_PATH + "/**";

        http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(antMatcher(HttpMethod.POST, AppConstants.LOGIN_PATH)).permitAll()
                        .requestMatchers(antMatcher("/error")).permitAll()
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .requestMatchers(antMatcher(AppConstants.AUTH_PATH + "/**")).authenticated()
                        .requestMatchers(antMatcher(HttpMethod.GET, contracts))
                        .hasAnyRole(ADMIN, Role.READ_CONTRACT.name())
                        .requestMatchers(antMatcher(HttpMethod.POST, contracts))
                        .hasAnyRole(ADMIN, Role.WRITE_CONTRACT.name())
                        .requestMatchers(antMatcher(HttpMethod.GET, invoices))
                        .hasAnyRole(ADMIN, Role.READ_INVOICE.name())
                        .requestMatchers(antMatcher(HttpMethod.POST, invoices))
                        .hasAnyRole(ADMIN, Role.WRITE_INVOICE.name())
                        .anyRequest().hasRole(ADMIN))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
