package com.example.demo.dtos;

public class AccountDetailsDTO {

    private String balance;

    private String accountNumber;


    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountDetailsDTO(String balance, String accountNumber) {
        this.balance = balance;
        this.accountNumber = accountNumber;
    }
}
