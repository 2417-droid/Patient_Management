package com.pm.billingservice.repository;

import com.pm.billingservice.model.BillingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BillingAccountRepository extends JpaRepository<BillingAccount, UUID> {

    // Used by the Kafka consumer to locate the account to soft-delete
    // when a Patient_Deleted event arrives. patientId is unique per account.
    Optional<BillingAccount> findByPatientId(UUID patientId);

    void deleteByPatientId(UUID patientId);// for hard delete

}
