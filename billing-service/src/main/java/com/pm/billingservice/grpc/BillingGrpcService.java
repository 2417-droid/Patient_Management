package com.pm.billingservice.grpc;


import billing.BillingResponse;
import billing.BillingServiceGrpc;
import com.pm.billingservice.model.BillingAccount;
import com.pm.billingservice.repository.BillingAccountRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);
    private final BillingAccountRepository billingAccountRepository;
    public BillingGrpcService(BillingAccountRepository billingAccountRepository) {
        this.billingAccountRepository = billingAccountRepository;
    }

    @Override
    public void createBillingAccount(billing.BillingRequest billingRequest , StreamObserver<billing.BillingResponse> responseObserver) {
        log.info("createBillingAccount request received {}", billingRequest.toString());

        BillingAccount newAccount = new BillingAccount();
        newAccount.setPatientId(UUID.fromString(billingRequest.getPatientId()));
        newAccount = billingAccountRepository.save(newAccount);

        BillingResponse response =  BillingResponse.newBuilder()
                .setAccountId(newAccount.getId().toString())
                .setStatus(newAccount.getStatus().name())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
