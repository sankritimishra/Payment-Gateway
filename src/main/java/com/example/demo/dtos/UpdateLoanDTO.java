package com.example.demo.dtos;

public class UpdateLoanDTO {
    private double totalLoanPaid;
    private double totalOutstanding;

    public UpdateLoanDTO(double totalLoanPaid, double totalOutstanding) {
        this.totalLoanPaid = totalLoanPaid;
        this.totalOutstanding = totalOutstanding;
    }

    public double getTotalLoanPaid() {
        return totalLoanPaid;
    }

    public void setTotalLoanPaid(double totalLoanPaid) {
        this.totalLoanPaid = totalLoanPaid;
    }

    public double getTotalOutstanding() {
        return totalOutstanding;
    }

    public void setTotalOutstanding(double totalOutstanding) {
        this.totalOutstanding = totalOutstanding;
    }
}
