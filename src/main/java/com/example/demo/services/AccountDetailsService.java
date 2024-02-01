package com.example.demo.services;

import com.example.demo.dtos.AccountDetailsDTO;
import com.example.demo.repositories.AccountDetailsRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountDetailsService {

    public AccountDetailsService(AccountDetailsRepository accountDetailsRepository) {
        this.accountDetailsRepository = accountDetailsRepository;
    }

    private final AccountDetailsRepository accountDetailsRepository;
    public String showBalance(String accountNumber) {
           return accountDetailsRepository.getAccountBalance(accountNumber);
    }


    public void updateBalance(String accountNumber, AccountDetailsDTO body) {
         accountDetailsRepository.updateAccountBalance(accountNumber,body);
    }

    public void addAccountDetails(String accountNumber, AccountDetailsDTO body) {
        accountDetailsRepository.addDetailsByAccountNumber(accountNumber, body);
    }
}
