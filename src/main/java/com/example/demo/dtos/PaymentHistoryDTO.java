package com.example.demo.dtos;

public class PaymentHistoryDTO {
    private String sourceAccountNumber;
    private String sourceAccountName;
    private String destAccountNumber;
    private String destAccountName;
    private double amount;

    public PaymentHistoryDTO(String sourceAccountNumber, String sourceAccountName, String destAccountNumber, String destAccountName, double amount, boolean isSuccessful, String messageSentToSourceAccount, String messageSentToDestAccount) {
        this.sourceAccountNumber = sourceAccountNumber;
        this.sourceAccountName = sourceAccountName;
        this.destAccountNumber = destAccountNumber;
        this.destAccountName = destAccountName;
        this.amount = amount;
        this.isSuccessful = isSuccessful;
        this.messageSentToSourceAccount = messageSentToSourceAccount;
        this.messageSentToDestAccount = messageSentToDestAccount;
    }

    private boolean isSuccessful;

    private String messageSentToSourceAccount;
    private String messageSentToDestAccount;

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getSourceAccountName() {
        return sourceAccountName;
    }

    public void setSourceAccountName(String sourceAccountName) {
        this.sourceAccountName = sourceAccountName;
    }

    public String getDestAccountNumber() {
        return destAccountNumber;
    }

    public void setDestAccountNumber(String destAccountNumber) {
        this.destAccountNumber = destAccountNumber;
    }

    public String getDestAccountName() {
        return destAccountName;
    }

    public void setDestAccountName(String destAccountName) {
        this.destAccountName = destAccountName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public String getMessageSentToSourceAccount() {
        return messageSentToSourceAccount;
    }

    public void setMessageSentToSourceAccount(String messageSentToSourceAccount) {
        this.messageSentToSourceAccount = messageSentToSourceAccount;
    }

    public String getMessageSentToDestAccount() {
        return messageSentToDestAccount;
    }

    public void setMessageSentToDestAccount(String messageSentToDestAccount) {
        this.messageSentToDestAccount = messageSentToDestAccount;
    }
}
