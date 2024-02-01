package com.example.demo.controllers;

import com.example.demo.dtos.AccountDetailsDTO;
import com.example.demo.services.AccountDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserAccountDetailsController {

    public UserAccountDetailsController(AccountDetailsService accountDetailsService) {
        this.accountDetailsService = accountDetailsService;
    }

    private final AccountDetailsService accountDetailsService;

    @GetMapping("/account/info/{account_number}")
    public String getBalanceByAccountNumber(@PathVariable("account_number") String accountNumber){
         return accountDetailsService.showBalance(accountNumber);
    }

    @PostMapping("/account/details/{account_number}")
    public void addAccountDetails(@PathVariable("account_number") String accountNumber, @RequestBody AccountDetailsDTO body){
        accountDetailsService.addAccountDetails(accountNumber, body);
    }

    @PostMapping("/account/updatebalance/{account_number}")
    public void updateBalanceByAccountNumber(@PathVariable("account_number") String accountNumber, @RequestBody  AccountDetailsDTO body){
        accountDetailsService.updateBalance(accountNumber, body);
    }

}
