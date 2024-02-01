package com.example.demo.controllers;

import com.example.demo.dtos.LoanDTO;
import com.example.demo.dtos.UpdateLoanDTO;
import com.example.demo.services.LoanService;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
public class LoanController {

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    private final LoanService loanService;
       @GetMapping("/loan/details/{account_number}")
       public LoanDTO getLoanDetailsByAccountNumber(@PathVariable("account_number") String accountNumber){
           //gives all the details of the table by account number
           return loanService.userLoanDetails(accountNumber);
       }

       @PostMapping("add/loan/details/{account_number}")
       public void addLoanDetailsByAccountNumber(@PathVariable String accountNumber, @RequestBody LoanDTO body) throws ParseException {
           loanService.addUserLoanDetails(accountNumber, body);
       }

       @PostMapping("/update/loan/details/{account_number}")
       public void updateLoanDetailsByAccountNumber(@PathVariable("account_number") String accountNumber) throws ParseException {
          // LoanDTO body = getLoanDetailsByAccountNumber(accountNumber);
           //System.out.println(paymentMade.getTotalLoanPaid());
          loanService.updateUserLoanDetails(accountNumber);
       }

}
