package com.pm.billingservice.model;


import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class BillingAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    public BillingStatus getStatus() {
        return status;
    }

    public void setStatus(BillingStatus status) {
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Enumerated(EnumType.STRING)
    @Column
    private BillingStatus status = BillingStatus.PENDING;

    @Column(nullable = false, unique = true)
    private UUID patientId;

    // Soft-delete flag. When true, this account is logically deleted
    // because the associated patient was deleted. The row is retained for audit history.
    @Column(nullable = false)
    private boolean deleted = false;

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
