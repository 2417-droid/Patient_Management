package com.pm.analyticsservice;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pm.analyticsservice.model.PatientRegistrationCount;
import com.pm.analyticsservice.repository.PatientRegistrationCountsRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRegistrationCountsRepository countRepository;

    @BeforeEach
    void setUp() {
        countRepository.deleteAll();

        PatientRegistrationCount count = new PatientRegistrationCount();
        count.setRegistrationDate(LocalDate.now());
        count.setCount(3L);
        countRepository.save(count);
    }

    @Test
    void summaryReturnsRegistrationCountsForLastThirtyDays() throws Exception {
        mockMvc.perform(get("/analytics/patients/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$[0].count").value(3));
    }
}
