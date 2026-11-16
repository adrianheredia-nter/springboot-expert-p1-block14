package com.nter.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nter.billing.config.InitializeData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthorizationTest {

    private static final String NEW_CONTRACT = """
            {"contractNumber": "CT-2025-100", "customerName": "Northwind", "startDate": "2025-01-01"}
            """;
    private static final String NEW_INVOICE = """
            {"invoiceNumber": "INV-2025-100", "issueDate": "2025-01-31", "amount": 99.90, "contractId": 1}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest(name = "{0} GET {1} -> {2}")
    @CsvSource({
            "admin, /api/contracts, 200",
            "contract.reader, /api/contracts, 200",
            "contract.reader, /api/contracts/1, 200",
            "contract.writer, /api/contracts, 403",
            "invoice.reader, /api/contracts, 403",
            "admin, /api/invoices, 200",
            "invoice.reader, /api/invoices, 200",
            "invoice.reader, /api/invoices?contractId=1, 200",
            "invoice.writer, /api/invoices, 403",
            "contract.reader, /api/invoices, 403"
    })
    void getAccess(String username, String path, int expectedStatus) throws Exception {
        mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, bearer(username)))
                .andExpect(status().is(expectedStatus));
    }

    @ParameterizedTest(name = "{0} POST contract -> {1}")
    @CsvSource({"admin, 201", "contract.writer, 201", "contract.reader, 403", "invoice.writer, 403"})
    void createContractAccess(String username, int expectedStatus) throws Exception {
        mockMvc.perform(post("/api/contracts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_CONTRACT))
                .andExpect(status().is(expectedStatus));
    }

    @ParameterizedTest(name = "{0} POST invoice -> {1}")
    @CsvSource({"admin, 201", "invoice.writer, 201", "invoice.reader, 403", "contract.writer, 403"})
    void createInvoiceAccess(String username, int expectedStatus) throws Exception {
        mockMvc.perform(post("/api/invoices")
                        .header(HttpHeaders.AUTHORIZATION, bearer(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_INVOICE))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/contracts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("A valid token is required"));
    }

    @Test
    void createdInvoiceIsLinkedToItsContract() throws Exception {
        mockMvc.perform(post("/api/invoices")
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_INVOICE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.contractId").value(1));
    }

    @Test
    void duplicatedContractNumberIsConflict() throws Exception {
        mockMvc.perform(post("/api/contracts")
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contractNumber": "CT-2024-001", "customerName": "Acme", "startDate": "2024-01-01"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void unknownContractIsNotFound() throws Exception {
        mockMvc.perform(get("/api/contracts/999").header(HttpHeaders.AUTHORIZATION, bearer("admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contract 999 not found"));
    }

    private String bearer(String username) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}"
                                .formatted(username, InitializeData.DEFAULT_PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(body).get("token").asText();
    }
}
