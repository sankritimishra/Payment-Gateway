package com.example.demo.repositories;

import com.example.demo.dtos.LoanDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class LoanRepository {

    public LoanRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    public LoanDTO getUserLoanDetails(String accountNumber) {
        String sql = "select * from loan where account_number= :accountNumber;";
        Map<String,String> mp = new HashMap<>();
        mp.put("accountNumber", accountNumber);
        var loanDetails = namedParameterJdbcTemplate.queryForList(sql,mp).get(0);
        return new LoanDTO(loanDetails.get("account_number").toString(),Double.parseDouble(loanDetails.get("loan_amount").toString()), Double.parseDouble(loanDetails.get("interst_rate").toString()), Double.parseDouble(loanDetails.get("tenure").toString()), Double.parseDouble(loanDetails.get("total_loan_payable").toString()), (loanDetails.get("start_date").toString()), Double.parseDouble(loanDetails.get("emi").toString()), Double.parseDouble(loanDetails.get("loan_paid").toString()), Double.parseDouble(loanDetails.get("total_outstanding").toString()));
    }

    public void addUserDetails(String accountNumber, LoanDTO body) {
        String sql = "insert into loan(account_number,loan_amount,interst_rate,tenure, total_loan_payable, start_date, emi, loan_paid, total_outstanding) values(:accountNumber, :loanAmount, :interestRate, :tenure, :totalLoanPayable, :startDate, :emi, :totalLoanPaid, :totalOutstanding);";
        Map<String,Object> mp = new HashMap<>();
        mp.put("accountNumber",accountNumber);
        mp.put("loanAmount", body.getLoanAmount());
        mp.put("interestRate", body.getInterestRate());
        mp.put("tenure", body.getTenure());
        mp.put("totalLoanPayable", body.getTotalLoanPayable());
        mp.put("startDate", body.getStartDate());
        mp.put("emi",body.getEmi());
        mp.put("totalLoanPaid",body.getTotalLoanPaid());
        mp.put("totalOutstanding", body.getTotalOutstanding());


        namedParameterJdbcTemplate.update(sql,mp);

    }

    public void updateUserDetails(String accountNumber, LoanDTO info) {
       String sql = "update loan set loan_paid = :totalLoanPaid, total_outstanding = :totalOutstanding where account_number= :accountNumber;";
       Map<String,Object> mp = new HashMap<>();
       mp.put("accountNumber", accountNumber);
       mp.put("totalLoanPaid", info.getTotalLoanPaid());
       mp.put("totalOutstanding", info.getTotalOutstanding());
       namedParameterJdbcTemplate.update(sql,mp);
    }

}
