package com.pm.patientservice;

import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.kafkaProducer;
import com.pm.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @MockitoBean
    private BillingServiceGrpcClient billingServiceGrpcClient;

    @MockitoBean
    private kafkaProducer kafkaProducer;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
    }

    @Test
    void createPatientPersistsPatientAndPublishesIntegrations() throws Exception {
        mockMvc.perform(post("/patients")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Integration Patient",
                                  "email": "patient.integration@example.com",
                                  "address": "123 Test Street",
                                  "birthDate": "1990-01-01",
                                  "registeredDate": "2026-10-11"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matchesPattern(
                        "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")))
                .andExpect(jsonPath("$.email").value("patient.integration@example.com"))
                .andExpect(jsonPath("$.name").value("Integration Patient"));

        org.junit.jupiter.api.Assertions.assertEquals(1, patientRepository.count());
        verify(billingServiceGrpcClient).createBillingAccount(anyString(),
                org.mockito.ArgumentMatchers.eq("Integration Patient"),
                org.mockito.ArgumentMatchers.eq("patient.integration@example.com"));
        verify(kafkaProducer).sendEvent(any());
    }
}
