package com.pm.patientservice.service;


import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.kafkaProducer;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final kafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository , BillingServiceGrpcClient wow, kafkaProducer kafkaProducer) {// it receives the repo
        this.billingServiceGrpcClient = wow;
        this.patientRepository = patientRepository;
        this.kafkaProducer = kafkaProducer;
    }
    public Page<PatientResponseDTO> getPatients(String search, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Patient> patientPage;
        if (search == null || search.trim().isEmpty()) {
            patientPage = patientRepository.findAll(pageable);
        } else {
            patientPage = patientRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search.trim(), search.trim(), pageable);
        }
        return patientPage.map(PatientMapper::toDTO);
    }
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
//        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
//            return null;
//        }
        Patient p = patientRepository.save(PatientMapper.toModel(patientRequestDTO));
        billingServiceGrpcClient.createBillingAccount(p.getPatientId().toString() , p.getName(), p.getEmail());

        kafkaProducer.sendEvent(p);

        //        System.out.println("Service response: " + response);

        return PatientMapper.toDTO(p);
    }
    public PatientResponseDTO updatePatient(PatientRequestDTO patientRequestDTO , UUID id){
        Patient p = patientRepository.findById(id).orElse(null);
        assert p != null;

        p.setName(patientRequestDTO.getName());
        p.setEmail(patientRequestDTO.getEmail());
        p.setAddress(patientRequestDTO.getAddress());
        p.setDateOfBirth(LocalDate.parse(patientRequestDTO.getBirthDate()));

        Patient updated = patientRepository.save(p);
        return PatientMapper.toDTO(updated);
    }
    @Transactional
    public void deletePatient(UUID id) {
        // Verify the patient exists before deleting.
        // deleteById() would silently do nothing on a missing ID;
        // we explicitly throw 404 so the event is never published for non-existent patients.
        if (!patientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found: " + id);
        }

        patientRepository.deleteById(id);

        // Only reached if the delete above succeeded.
        // Billing-service will consume this and soft-delete the associated BillingAccount.
        kafkaProducer.sendDeleteEvent(id);
    }
}
