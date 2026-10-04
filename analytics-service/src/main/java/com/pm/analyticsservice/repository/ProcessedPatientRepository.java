package com.pm.analyticsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedPatient extends JpaRepository<PatientRegistrationCounts, Long> {
}
