package com.pm.patientservice.service;


import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    public PatientService(PatientRepository patientRepository , BillingServiceGrpcClient wow) {// it receives the repo
        this.billingServiceGrpcClient = wow;
        this.patientRepository = patientRepository;
    }
    public List<PatientResponseDTO> getPatients(){
        List<Patient> patientList = patientRepository.findAll();
        return patientList.stream().map(
                PatientMapper::toDTO
        ).toList();
    }
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
//        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
//            return null;
//        }
        Patient p = patientRepository.save(PatientMapper.toModel(patientRequestDTO));
        billingServiceGrpcClient.createBillingAccount(p.getPatientId().toString() , p.getName(), p.getEmail());

        PatientResponseDTO response = PatientMapper.toDTO(p);

        System.out.println("Service response: " + response);

        return response;
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
    public void deletePatient(UUID id){
        patientRepository.deleteById(id);
    }
}
