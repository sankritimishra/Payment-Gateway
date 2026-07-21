package com.example.demo.controllers;

import com.example.demo.dtos.PaymentHistoryDTO;
import com.example.demo.services.PaymentHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;

    public PaymentHistoryController(PaymentHistoryService paymentHistoryService) {
        this.paymentHistoryService = paymentHistoryService;
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentHistoryDTO> makePayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PaymentHistoryDTO body) throws Exception {

        // DuplicateRequestInProgressException, InsufficientFundsException, etc. are
        // handled centrally by GlobalExceptionHandler - no need to catch them here.
        PaymentHistoryDTO result = paymentHistoryService.makeTransaction(
                idempotencyKey, body.getSourceAccountNumber(), body);
        return ResponseEntity.ok(result);
    }
}
