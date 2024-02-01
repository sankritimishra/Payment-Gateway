package com.example.demo.dtos;

public class LoanDTO {

    private String accountNumber;
    private Double loanAmount;
    private Double interestRate;

    private Double tenure;
    private Double totalLoanPayable;
    private String startDate;
    private Double emi;
    private Double totalLoanPaid;
    private Double totalOutstanding;

    public LoanDTO(String accountNumber, Double loanAmount, Double interestRate, Double tenure, Double totalLoanPayable, String startDate, Double emi, Double totalLoanPaid, Double totalOutstanding) {
        this.accountNumber = accountNumber;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.tenure = tenure;
        this.totalLoanPayable = totalLoanPayable;
        this.startDate = startDate;
        this.emi = emi;
        this.totalLoanPaid = totalLoanPaid;
        this.totalOutstanding = totalOutstanding;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(Double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Double getTenure() {
        return tenure;
    }

    public void setTenure(Double tenure) {
        this.tenure = tenure;
    }

    public Double getTotalLoanPayable() {
        return totalLoanPayable;
    }

    public void setTotalLoanPayable(Double totalLoanPayable) {
        this.totalLoanPayable = totalLoanPayable;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Double getEmi() {
        return emi;
    }

    public void setEmi(Double emi) {
        this.emi = emi;
    }

    public Double getTotalLoanPaid() {
        return totalLoanPaid;
    }

    public void setTotalLoanPaid(Double totalLoanPaid) {
        this.totalLoanPaid = totalLoanPaid;
    }

    public Double getTotalOutstanding() {
        return totalOutstanding;
    }

    public void setTotalOutstanding(Double totalOutstanding) {
        this.totalOutstanding = totalOutstanding;
    }
}
