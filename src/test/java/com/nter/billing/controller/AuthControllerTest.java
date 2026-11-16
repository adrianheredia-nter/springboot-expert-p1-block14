package com.nter.billing.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nter.billing.config.InitializeData;
import com.nter.billing.entity.UserJPA;
import com.nter.billing.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void loginReturnsBearerToken() throws Exception {
        mockMvc.perform(login("admin", InitializeData.DEFAULT_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(10800));
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        mockMvc.perform(login("admin", "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void loginWithUnknownUserIsUnauthorized() throws Exception {
        mockMvc.perform(login("ghost", InitializeData.DEFAULT_PASSWORD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshReturnsANewTokenForTheLoggedUser() throws Exception {
        String token = tokenFor("contract.reader");

        String body = mockMvc.perform(post("/auth/refresh").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String refreshed = objectMapper.readTree(body).get("token").asText();
        assertThat(refreshed).isNotEqualTo(token);
        assertThat(payload(refreshed).get("sub").asText()).isEqualTo("contract.reader");
    }

    @Test
    void bearerSchemeIsCaseInsensitive() throws Exception {
        mockMvc.perform(post("/auth/refresh").header(HttpHeaders.AUTHORIZATION, "bearer " + tokenFor("admin")))
                .andExpect(status().isOk());
    }

    @Test
    void refreshWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithForgedPayloadIsUnauthorized() throws Exception {
        String[] parts = tokenFor("contract.reader").split("\\.");
        String forgedPayload = new String(Base64.getUrlDecoder().decode(parts[1])).replace("contract.reader", "admin");
        String forged = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(forgedPayload.getBytes()) + "." + parts[2];

        mockMvc.perform(post("/auth/refresh").header(HttpHeaders.AUTHORIZATION, "Bearer " + forged))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredUserChangesPasswordAndLosesTheExpiredFlag() throws Exception {
        String token = tokenFor("expired.user");

        mockMvc.perform(changePassword(token, "Brand.New.2024"))
                .andExpect(status().isNoContent());

        UserJPA user = userRepository.findByUsername("expired.user").orElseThrow();
        assertThat(user.isPasswordExpired()).isFalse();
        assertThat(passwordEncoder.matches("Brand.New.2024", user.getPassword())).isTrue();
        mockMvc.perform(login("expired.user", "Brand.New.2024")).andExpect(status().isOk());
        mockMvc.perform(changePassword(token, "Another.One.2024")).andExpect(status().isForbidden());
    }

    @Test
    void adminCanChangeItsPassword() throws Exception {
        mockMvc.perform(changePassword(tokenFor("admin"), "Admin.New.2024"))
                .andExpect(status().isNoContent());
    }

    @Test
    void userWithoutExpiredPasswordCannotChangeIt() throws Exception {
        mockMvc.perform(changePassword(tokenFor("contract.reader"), "Reader.New.2024"))
                .andExpect(status().isForbidden());
    }

    @Test
    void newPasswordMustBeDifferent() throws Exception {
        mockMvc.perform(changePassword(tokenFor("expired.user"), InitializeData.DEFAULT_PASSWORD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The new password must be different from the current one"));
    }

    private RequestBuilder login(String username, String password) {
        return post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password));
    }

    private RequestBuilder changePassword(String token, String newPassword) {
        return post("/auth/user/changepass")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"%s\"}".formatted(newPassword));
    }

    private String tokenFor(String username) throws Exception {
        String body = mockMvc.perform(login(username, InitializeData.DEFAULT_PASSWORD))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private JsonNode payload(String token) throws Exception {
        String payload = token.split("\\.")[1];
        return objectMapper.readTree(Base64.getUrlDecoder().decode(payload));
    }
}
