package com.pm.analyticsservice.model;


import jakarta.persistence.*;

@Entity
@Table(name = "processed_patients")
public class ProcessedPatient {

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    @Id
    private String patientId;
}
