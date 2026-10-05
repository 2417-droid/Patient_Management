package com.pm.billingservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pm.billingservice.model.BillingAccount;
import com.pm.billingservice.repository.BillingAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

import java.util.Optional;
import java.util.UUID;

@Service
public class BillingKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(BillingKafkaConsumer.class);

    private final BillingAccountRepository billingAccountRepository;

    public BillingKafkaConsumer(BillingAccountRepository billingAccountRepository) {
        this.billingAccountRepository = billingAccountRepository;
    }

    @KafkaListener(topics = "patient", groupId = "billing-service")
    public void consumeEvent(byte[] event) {
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);

            // Only act on deletion events — all other event types are ignored silently.
            // This keeps the consumer open to future event types without crashing.
            if (!"Patient_Deleted".equals(patientEvent.getEventType())) {
                return;
            }

            String patientIdStr = patientEvent.getId();

            // Validate the ID is non-empty before parsing
            if (patientIdStr == null || patientIdStr.isBlank()) {
                log.error("Received Patient_Deleted event with blank patientId — skipping");
                return;
            }

            UUID patientId;
            try {
                patientId = UUID.fromString(patientIdStr);
            } catch (IllegalArgumentException e) {
                log.error("Received Patient_Deleted event with malformed patientId='{}' — skipping", patientIdStr);
                return;
            }

            // Look up the billing account by patientId (NOT by name or email)
            Optional<BillingAccount> accountOpt = billingAccountRepository.findByPatientId(patientId);

            if (accountOpt.isEmpty()) {
                // This can happen if the billing account was never created (e.g. gRPC failure
                // at registration).
                // Log a warning and move on — do not throw, which would cause Kafka to retry
                // endlessly.
                log.warn("No BillingAccount found for patientId={} — nothing to soft-delete", patientId);
                return;
            }

            BillingAccount account = accountOpt.get();
            // Delete the billing account (soft delete)
            // For hard delete
            // billingAccountRepository.deleteByPatientId(patientId);
            if (account.isDeleted()) {
                // Idempotency: if already soft-deleted (duplicate event delivery), skip.
                log.warn("BillingAccount for patientId={} is already soft-deleted — skipping duplicate event",
                        patientId);
                return;
            }

            // Soft-delete: mark as deleted without removing the row.
            // The row is retained for billing history and audit purposes.
            account.setDeleted(true);
            billingAccountRepository.save(account);

            log.info("Soft-deleted BillingAccount for patientId={}", patientId);
            // Do NOT log account ID, name, email, or any other patient PII here.

        } catch (InvalidProtocolBufferException e) {
            // Malformed Protobuf bytes — log and discard.
            // Do NOT re-throw: re-throwing would cause Kafka to retry this bad message
            // infinitely.
            log.error("Failed to deserialize Kafka event in billing-service: {}", e.getMessage());
        }
    }
}
