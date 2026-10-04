package com.pm.patientservice.kafka;

import com.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

import java.time.LocalDate;

@Service
public class kafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(kafkaProducer.class);
    private final KafkaTemplate<String , byte[]> kafkaTemplate;
    public kafkaProducer(KafkaTemplate<String , byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void sendEvent(Patient patient){
        PatientEvent patientEvent = PatientEvent.newBuilder()
                .setId(patient.getPatientId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setRegisteredDate(patient.getRegisteredDate().toString())
                .setEventType("Patient_Created")
                .build();

        try {
            kafkaTemplate.send("patient" , patientEvent.toByteArray());
        }
        catch (Exception e){
            log.error("Error while sending Patient Event {}",  patientEvent);
        }
    }
}
