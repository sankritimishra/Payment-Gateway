package com.example.demo.dtos;

import java.math.BigDecimal;

public class PaymentHistoryDTO {

    private String sourceAccountNumber;
    private String sourceAccountName;
    private String destAccountNumber;
    private String destAccountName;
    private BigDecimal amount;
    private boolean successful;
    private String messageSentToSourceAccount;
    private String messageSentToDestAccount;

    public PaymentHistoryDTO() {
    }

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
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