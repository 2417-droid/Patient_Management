package com.pm.patientservice.kafka;

import com.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

import java.util.UUID;

@Service
public class kafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(kafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public kafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Patient patient) {
        PatientEvent patientEvent = PatientEvent.newBuilder()
                .setId(patient.getPatientId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setRegisteredDate(patient.getRegisteredDate().toString())
                .setEventType("Patient_Created")
                .build();

        try {
            kafkaTemplate.send("patient", patientEvent.toByteArray());
        } catch (Exception e) {
            // Do not log the event object itself — it contains PII (email)
            log.error("Error while sending Patient_Created event: {}", e.getMessage());
        }
    }

    // Publishes a minimal Patient_Deleted event.
    // Only patientId and event_type are set — no PII required by any consumer.
    // IMPORTANT: This must only be called after a confirmed successful deletion.
    public void sendDeleteEvent(UUID patientId) {
        PatientEvent patientEvent = PatientEvent.newBuilder()
                .setId(patientId.toString())
                .setEventType("Patient_Deleted")
                .build();

        try {
            kafkaTemplate.send("patient", patientEvent.toByteArray());
            log.info("Published Patient_Deleted event for patientId={}", patientId);
        } catch (Exception e) {
            log.error("Error sending Patient_Deleted event for patientId={}: {}", patientId, e.getMessage());
        }
    }
}
