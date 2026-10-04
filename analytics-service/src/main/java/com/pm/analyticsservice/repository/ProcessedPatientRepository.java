package com.pm.analyticsservice.repository;

import com.pm.analyticsservice.model.ProcessedPatient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedPatientRepository
        extends JpaRepository<ProcessedPatient, String> {
}