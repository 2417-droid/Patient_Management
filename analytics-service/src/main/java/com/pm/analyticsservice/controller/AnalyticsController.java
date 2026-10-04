package com.pm.analyticsservice.controller;

import com.pm.analyticsservice.model.PatientRegistrationCount;
import com.pm.analyticsservice.repository.PatientRegistrationCountsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final PatientRegistrationCountsRepository countRepository;

    public AnalyticsController(PatientRegistrationCountsRepository countRepository) {
        this.countRepository = countRepository;
    }

    /**
     * Returns daily patient registration counts for the last 30 days (UTC dates).
     * GET /analytics/patients/summary
     */
    @GetMapping("/patients/summary")
    public ResponseEntity<List<PatientRegistrationCount>> getSummary() {
        LocalDate endDate = LocalDate.now(); // UTC today
        LocalDate startDate = endDate.minusDays(29); // last 30 days inclusive

        List<PatientRegistrationCount> summary = countRepository.findAllByRegistrationDateBetween(startDate, endDate);

        return ResponseEntity.ok(summary);
    }
}
