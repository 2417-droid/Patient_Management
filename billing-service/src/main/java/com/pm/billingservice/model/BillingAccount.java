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

    @Column(nullable = false)
    private UUID patientId;

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
}
