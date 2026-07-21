package com.example.demo.dtos;

import java.math.BigDecimal;

public class AccountDetailsDTO {

    private BigDecimal balance;

    private String accountNumber;

    public AccountDetailsDTO() {
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountDetailsDTO(BigDecimal balance, String accountNumber) {
        this.balance = balance;
        this.accountNumber = accountNumber;
    }
}
