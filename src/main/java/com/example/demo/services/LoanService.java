package com.example.demo.services;

import com.example.demo.dtos.LoanDTO;
import com.example.demo.repositories.LoanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class LoanService {

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    private final LoanRepository loanRepository;

    public void calDetails(LoanDTO body) {
        double principal = body.getLoanAmount();
        double interest = body.getInterestRate()/12/100;
        double t = body.getTenure()*12;
        double payLoan = principal * (1 + interest*t);
        body.setTotalLoanPayable(payLoan);
        double emi = (principal*interest*Math.pow(1+interest,t))/(Math.pow(1+interest,t)-1);
        body.setEmi(emi);

        // Compute "today" at call time, not at service construction time, so this
        // reflects the actual request date rather than the app's startup date.
        LocalDate startDate = LocalDate.parse(body.getStartDate());
        LocalDate today = LocalDate.now();
        Period diff = Period.between(startDate, today);
        double loanPaid = (emi * diff.getMonths());
        body.setTotalLoanPaid(loanPaid);
        double outstanding = payLoan - loanPaid;
        body.setTotalOutstanding(outstanding);
    }
    public LoanDTO userLoanDetails(String accountNumber) {
        return loanRepository.getUserLoanDetails(accountNumber);
    }

    public void addUserLoanDetails(String accountNumber, LoanDTO body) {
        calDetails(body);
        loanRepository.addUserDetails(accountNumber,body);
    }

    public void updatePayment(LoanDTO info){
        info.setTotalLoanPaid(info.getTotalLoanPaid() + info.getEmi());
        info.setTotalOutstanding(info.getTotalOutstanding() - info.getEmi());
    }
    public void updateUserLoanDetails(String accountNumber) {
        LoanDTO info = loanRepository.getUserLoanDetails(accountNumber);
        updatePayment(info);
        loanRepository.updateUserDetails(accountNumber,info);
    }


}
