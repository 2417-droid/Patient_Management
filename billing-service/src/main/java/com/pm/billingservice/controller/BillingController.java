package com.pm.billingservice.controller;

import com.pm.billingservice.model.BillingAccount;
import com.pm.billingservice.model.BillingStatus;
import com.pm.billingservice.repository.BillingAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/billing/accounts")
public class BillingController {
    private final BillingAccountRepository repository;
    public BillingController(BillingAccountRepository repository) {
        this.repository = repository;
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<BillingAccount> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        BillingAccount account = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Billing account not found"));
        BillingStatus newStatus;
        try {
            newStatus = BillingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid billing status: " + status);
        }
        account.setStatus(newStatus);
        BillingAccount updatedAccount = repository.save(account);
        return ResponseEntity.ok(updatedAccount);
    }
}
