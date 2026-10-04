package com.pm.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pm.analyticsservice.model.PatientRegistrationCount;
import com.pm.analyticsservice.model.ProcessedPatient;
import com.pm.analyticsservice.repository.PatientRegistrationCountsRepository;
import com.pm.analyticsservice.repository.PatientRegistrationCountsRepository;
import com.pm.analyticsservice.repository.ProcessedPatientRepository;
import com.pm.analyticsservice.repository.ProcessedPatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

import java.time.LocalDate;

@Service
public class kafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(kafkaConsumer.class);

    private final PatientRegistrationCountsRepository countRepository;
    private final ProcessedPatientRepository processedRepository;

    public kafkaConsumer(PatientRegistrationCountsRepository countRepository,
                         ProcessedPatientRepository processedRepository) {
        this.countRepository = countRepository;
        this.processedRepository = processedRepository;
    }

    @KafkaListener(topics = "patient", groupId = "analytics-service")
    public void consumeEvent(byte[] event) {
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);

            String patientId = patientEvent.getId();
            String eventType = patientEvent.getEventType();

            // Only count Patient_Created events
            if (!"Patient_Created".equals(eventType)) {
                log.debug("Skipping non-registration event type={}", eventType);
                return;
            }

            // Idempotency check: skip if this patientId has already been counted
            // Known limitation: a tiny race exists if two consumers process the same
            // patientId simultaneously before either persists — this does not claim
            // exactly-once semantics, but handles the common duplicate-delivery case.
            if (processedRepository.existsById(patientId)) {
                log.warn("Duplicate event skipped for patientId={}", patientId);
                return;
            }

            // Parse the registration date (UTC, ISO-8601 e.g. "2024-10-03")
            LocalDate registrationDate;
            try {
                registrationDate = LocalDate.parse(patientEvent.getRegisteredDate());
            } catch (Exception e) {
                log.error("Malformed registered_date for patientId={}, skipping event", patientId);
                return; // Safe error handling — do not re-throw so Kafka does not retry endlessly
            }

            // Increment daily count
            PatientRegistrationCount dayCount = countRepository
                    .findByRegistrationDate(registrationDate)
                    .orElseGet(() -> {
                        PatientRegistrationCount newDay = new PatientRegistrationCount();
                        newDay.setRegistrationDate(registrationDate);
                        newDay.setCount(0L);
                        return newDay;
                    });
            dayCount.setCount(dayCount.getCount() + 1);
            countRepository.save(dayCount);

            // Mark this patientId as processed
            ProcessedPatient processed = new ProcessedPatient();
            processed.setPatientId(patientId);
            processedRepository.save(processed);

            log.info("Counted registration for patientId={} on date={}", patientId, registrationDate);
            // Note: name, email are intentionally NOT logged

        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing Kafka event: {}", e.getMessage());
            // Don't re-throw — malformed events are discarded, not retried
        }
    }
}
