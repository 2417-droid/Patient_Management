package com.pm.analyticsservice.model;


import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "patient_registration_counts")
public class PatientRegistrationCount {

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false , unique = true)
    private LocalDate registrationDate;

    @Column(nullable = false)
    private long count = 0L;

}
