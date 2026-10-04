package com.pm.analyticsservice.repository;

import com.pm.analyticsservice.model.PatientRegistrationCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRegistrationCounts extends JpaRepository<PatientRegistrationCounts, Long> {
    Optional<PatientRegistrationCount> findByRegistrationDate(LocalDate date);
    List<PatientRegistrationCount> findAllByRegistrationDateBetween(LocalDate start, LocalDate end);
}
