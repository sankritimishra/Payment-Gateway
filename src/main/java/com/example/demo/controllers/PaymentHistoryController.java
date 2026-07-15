package com.example.demo.controllers;

import com.example.demo.dtos.PaymentHistoryDTO;
import com.example.demo.services.PaymentHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.sql.SQLException;

@RestController
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;

    public PaymentHistoryController(PaymentHistoryService paymentHistoryService) {
        this.paymentHistoryService = paymentHistoryService;
    }

    @PostMapping("/payments")
    public ResponseEntity<?> makePayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PaymentHistoryDTO body) throws Exception {

        try {
            PaymentHistoryDTO result = paymentHistoryService.makeTransaction(
                    idempotencyKey, body.getSourceAccountNumber(), body);
            return ResponseEntity.ok(result);
        } catch (PaymentHistoryService.DuplicateRequestInProgressException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
