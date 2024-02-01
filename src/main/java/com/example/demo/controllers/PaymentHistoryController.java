package com.example.demo.controllers;

import com.example.demo.dtos.PaymentHistoryDTO;
import com.example.demo.services.PaymentHistoryService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;

    public PaymentHistoryController(PaymentHistoryService paymentHistoryService) {
        this.paymentHistoryService = paymentHistoryService;
    }

    @PostMapping("/maketransaction/{sourceAccountNumber}")
    public void transaction(@PathVariable String sourceAccountNumber, @RequestBody PaymentHistoryDTO body) throws Exception {
        paymentHistoryService.makeTransaction(sourceAccountNumber, body);
    }


}
