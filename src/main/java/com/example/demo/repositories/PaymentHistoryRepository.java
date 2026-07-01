package com.example.demo.repositories;

import com.example.demo.dtos.PaymentHistoryDTO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Repository
@EnableRetry
public class PaymentHistoryRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final AccountDetailsRepository accountDetailsRepository;

    public PaymentHistoryRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                    AccountDetailsRepository accountDetailsRepository) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.accountDetailsRepository = accountDetailsRepository;
    }

    /**
     * Custom exception used to distinguish "business rule failure"
     * (e.g. insufficient funds) from transient/technical failures.
     * We do NOT want @Retryable to retry business failures.
     */
    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    /**
     * Updates both account balances atomically using parameterized queries.
     * Relies on Spring's transaction manager (via @Transactional on the caller)
     * rather than manually opening/committing/closing a JDBC connection.
     */
    private void updateBalances(String sourceAccountNumber, String destAccountNumber,
                                BigDecimal newSourceBalance, BigDecimal newDestBalance) {

        String updateSourceSql = "UPDATE user_account_details SET balance = :balance WHERE account_number = :accountNumber";
        String updateDestSql = "UPDATE user_account_details SET balance = :balance WHERE account_number = :accountNumber";

        MapSqlParameterSource sourceParams = new MapSqlParameterSource()
                .addValue("balance", newSourceBalance)
                .addValue("accountNumber", sourceAccountNumber);

        MapSqlParameterSource destParams = new MapSqlParameterSource()
                .addValue("balance", newDestBalance)
                .addValue("accountNumber", destAccountNumber);

        int sourceRowsUpdated = namedParameterJdbcTemplate.update(updateSourceSql, sourceParams);
        int destRowsUpdated = namedParameterJdbcTemplate.update(updateDestSql, destParams);

        if (sourceRowsUpdated == 0 || destRowsUpdated == 0) {
            // account_number didn't match any row — fail loudly instead of silently no-op'ing
            throw new IllegalStateException("Failed to update balances: one or both account numbers not found");
        }
    }

    private void insertHistoryRecord(String sourceAccountNumber, PaymentHistoryDTO body) {
        String sql = "INSERT INTO payment_history " +
                "(sourceAccountNumber, sourceAccountName, destAccountNumber, destAccountName, amount, " +
                "isSuccessful, messageSentToSourceAccount, messageSentToDestAccount) " +
                "VALUES (:sourceAccountNumber, :sourceAccountName, :destAccountNumber, :destAccountName, " +
                ":amount, :isSuccessful, :messageSentToSourceAccount, :messageSentToDestAccount)";

        Map<String, Object> params = new HashMap<>();
        params.put("sourceAccountNumber", sourceAccountNumber);
        params.put("sourceAccountName", body.getSourceAccountName());
        params.put("destAccountNumber", body.getDestAccountNumber());
        params.put("destAccountName", body.getDestAccountName());
        params.put("amount", body.getAmount());
        params.put("isSuccessful", body.isSuccessful());
        params.put("messageSentToSourceAccount", body.getMessageSentToSourceAccount());
        params.put("messageSentToDestAccount", body.getMessageSentToDestAccount());

        namedParameterJdbcTemplate.update(sql, params);
    }

    /**
     * @Recover is only invoked after all @Retryable attempts on Exception.class are exhausted.
     * It records the final failure — it never retries itself.
     */
    @Recover
    public void retryPayment(Exception e, String sourceAccountNumber, PaymentHistoryDTO body) {
        body.setSuccessful(false);
        body.setMessageSentToSourceAccount("Transaction failed, amount not deducted");
        body.setMessageSentToDestAccount("");
        insertHistoryRecord(sourceAccountNumber, body);
    }

    /**
     * Only retry on genuinely transient failures (e.g. transient DB connectivity issues).
     * We deliberately exclude InsufficientFundsException and IllegalArgumentException —
     * retrying a business-rule failure (like insufficient funds) will never succeed and
     * just wastes attempts / delays the user's error message.
     */
    @Transactional
    @Retryable(
            value = DataAccessException.class,
            maxAttempts = 3,
            exclude = {InsufficientFundsException.class, IllegalArgumentException.class}
    )
    public void makePayment(String sourceAccountNumber, PaymentHistoryDTO body) {
        String destAccountNumber = body.getDestAccountNumber();
        BigDecimal amount = body.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        BigDecimal sourceBalance = accountDetailsRepository.getAccountBalance(sourceAccountNumber);
        BigDecimal destBalance = accountDetailsRepository.getAccountBalance(destAccountNumber);

        if (sourceBalance == null || destBalance == null) {
            throw new IllegalArgumentException("Invalid source or destination account number");
        }

        if (sourceBalance.compareTo(amount) < 0) {
            body.setSuccessful(false);
            body.setMessageSentToSourceAccount("Transaction failed: insufficient funds");
            body.setMessageSentToDestAccount("");
            insertHistoryRecord(sourceAccountNumber, body);
            throw new InsufficientFundsException(
                    "Account " + sourceAccountNumber + " has insufficient funds for this transaction");
        }

        BigDecimal newSourceBalance = sourceBalance.subtract(amount);
        BigDecimal newDestBalance = destBalance.add(amount);

        updateBalances(sourceAccountNumber, destAccountNumber, newSourceBalance, newDestBalance);

        body.setSuccessful(true);
        body.setMessageSentToSourceAccount("Transaction successful, amount deducted");
        body.setMessageSentToDestAccount("Amount received");

        insertHistoryRecord(sourceAccountNumber, body);
    }
}