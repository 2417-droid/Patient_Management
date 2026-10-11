package com.pm.billingservice;

import com.pm.billingservice.model.BillingAccount;
import com.pm.billingservice.model.BillingStatus;
import com.pm.billingservice.repository.BillingAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BillingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BillingAccountRepository billingAccountRepository;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        billingAccountRepository.deleteAll();

        BillingAccount account = new BillingAccount();
        account.setPatientId(UUID.randomUUID());
        account.setStatus(BillingStatus.PENDING);
        accountId = billingAccountRepository.save(account).getId();
    }

    @Test
    void updateStatusChangesExistingBillingAccount() throws Exception {
        mockMvc.perform(patch("/billing/accounts/{id}/status", accountId)
                        .queryParam("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.status").value("PAID"));

        BillingAccount updated = billingAccountRepository.findById(accountId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(BillingStatus.PAID, updated.getStatus());
    }

    @Test
    void updateStatusRejectsUnknownStatus() throws Exception {
        mockMvc.perform(patch("/billing/accounts/{id}/status", accountId)
                        .queryParam("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }
}
