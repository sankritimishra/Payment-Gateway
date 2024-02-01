package com.example.demo.services;

import com.example.demo.dtos.LoanDTO;
import com.example.demo.dtos.UpdateLoanDTO;
import com.example.demo.repositories.LoanRepository;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;

@Service
public class LoanService {
    SimpleDateFormat ft
            = new SimpleDateFormat("yyyy-MM-dd");

    String str = ft.format(new Date());

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    private final LoanRepository loanRepository;

    public void calDetails(LoanDTO body) throws ParseException {
        double principal = body.getLoanAmount();
        double interest = body.getInterestRate()/12/100;
        double t = body.getTenure()*12;
        double payLoan = principal * (1 + interest*t);
        body.setTotalLoanPayable(payLoan);
        double emi = (principal*interest*Math.pow(1+interest,t))/(Math.pow(1+interest,t)-1);
        body.setEmi(emi);
        Date d1 = ft.parse(body.getStartDate());
        Date d2 = ft.parse(str);
        Period diff = Period.between(LocalDate.parse(body.getStartDate()), LocalDate.parse(str));
        double loanPaid = (emi * diff.getMonths());
        body.setTotalLoanPaid(loanPaid);
        double outstanding = payLoan - loanPaid;
        body.setTotalOutstanding(outstanding);
    }
    public LoanDTO userLoanDetails(String accountNumber) {
        return loanRepository.getUserLoanDetails(accountNumber);
    }

    public void addUserLoanDetails(String accountNumber, LoanDTO body) throws ParseException {

        calDetails(body);
        loanRepository.addUserDetails(accountNumber,body);
    }

    public void updatePayment(LoanDTO info){
        info.setTotalLoanPaid(info.getTotalLoanPaid() + info.getEmi());
        info.setTotalOutstanding(info.getTotalOutstanding() - info.getEmi());
    }
    public void updateUserLoanDetails(String accountNumber) throws ParseException {
        LoanDTO info = loanRepository.getUserLoanDetails(accountNumber);
        updatePayment(info);
        loanRepository.updateUserDetails(accountNumber,info);
    }


}
