package com.example.openbanking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requestWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithCorrectScopeShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .with(jwt().authorities(
                                () -> "SCOPE_accounts:read")))
                .andExpect(status().isOk());
    }

    @Test
    void requestWithWrongScopeShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .with(jwt().authorities(
                                () -> "SCOPE_payments:read")))
                .andExpect(status().isForbidden());
    }
}