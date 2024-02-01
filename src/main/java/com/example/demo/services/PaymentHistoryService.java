package com.example.demo.services;

import com.example.demo.dtos.AccountDetailsDTO;
import com.example.demo.dtos.PaymentHistoryDTO;
import com.example.demo.repositories.PaymentHistoryRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service

public class PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;

    public PaymentHistoryService(PaymentHistoryRepository paymentHistoryRepository) {
        this.paymentHistoryRepository = paymentHistoryRepository;
    }


    public void makeTransaction(String sourceAccountNumber, PaymentHistoryDTO body) throws Exception {
        paymentHistoryRepository.makePayment(sourceAccountNumber,body);
    }

}
