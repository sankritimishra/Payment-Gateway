package com.example.demo.repositories;

import com.example.demo.dtos.PaymentHistoryDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


@Repository
@EnableRetry
public class PaymentHistoryRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public PaymentHistoryRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, AccountDetailsRepository repository) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.repository = repository;
    }

    private final AccountDetailsRepository repository;

   public void transactionDetails(String sourceAccountNumber, PaymentHistoryDTO body, String newSourceBalance, String newDestBalance, String srcAcc, String destAcc) throws Exception {

        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/paymentdb","sankriti","mishra");
        try{
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            String beginTransaction = "start transaction;";
            String updateSource ="update user_account_details set balance =" + newSourceBalance + "where account_number=" + "1234567" + ";" ;
            String updateDest ="update user_account_details set balance =" + newDestBalance + "where account_number=" + destAcc + ";" ;
            String commitTransaction="commit;";
            statement.execute(beginTransaction);
            statement.execute(updateSource);
            statement.execute(updateDest);
            statement.execute(commitTransaction);
            body.setSuccessful(true);
            body.setMessageSentToSourceAccount("transaction successful, amount deducted");
            body.setMessageSentToDestAccount("amount recieved");
        }catch(Exception sqlException){
            connection.rollback();
            System.out.println("recover not called");
            throw new Exception();
        }
        connection.close();
   }

    @Recover
    public void retryPayment(Exception s,String sourceAccountNumber, PaymentHistoryDTO body){
        body.setSuccessful(false);
        body.setMessageSentToSourceAccount("transaction failed, amount not deducted");
        body.setMessageSentToDestAccount("");
        String sql = "insert into payment_history(sourceAccountNumber,sourceAccountName,destAccountNumber, destAccountName, amount, isSuccessful, messageSentToSourceAccount, messageSentToDestAccount) values (:sourceAccountNumber,:sourceAccountName, :destAccountNumber, :destAccountName, :amount, :isSuccessful, :messageSentToSourceAccount, :messageSentToDestAccount)";
        Map<String,Object> mp = new HashMap<>();
        mp.put("sourceAccountNumber", sourceAccountNumber);
        mp.put("sourceAccountName", body.getSourceAccountName());
        mp.put("destAccountNumber", body.getDestAccountNumber());
        mp.put("destAccountName", body.getDestAccountName());
        mp.put("amount", body.getAmount());
        mp.put("isSuccessful", body.isSuccessful());
        mp.put("messageSentToSourceAccount", body.getMessageSentToSourceAccount());
        mp.put("messageSentToDestAccount", body.getMessageSentToDestAccount());

        namedParameterJdbcTemplate.update(sql,mp);
        System.out.println("recover called");
    }


    @Transactional
    @Retryable(value = Exception.class)
    public void makePayment(String sourceAccountNumber, PaymentHistoryDTO body) throws Exception {
        String newSourceBalance, newDestBalance, srcAcc, destAcc;
        try {
            String sourceBalance = repository.getAccountBalance(sourceAccountNumber);
            String destinationBalance = repository.getAccountBalance(body.getDestAccountNumber());

            newSourceBalance = String.valueOf(Double.parseDouble(sourceBalance) - body.getAmount());
            newDestBalance = String.valueOf(Double.parseDouble(destinationBalance) + body.getAmount());

            srcAcc = body.getSourceAccountNumber();
            destAcc = body.getDestAccountNumber();
            System.out.println("we are here");
        } catch (Exception e) {
            body.setSuccessful(false);
            body.setMessageSentToSourceAccount("transaction failed, amount not deducted");
            body.setMessageSentToDestAccount("");
            System.out.println("Hey enter the correct account number");
            return;
        }
        transactionDetails(sourceAccountNumber, body, newSourceBalance, newDestBalance, srcAcc, destAcc);

        String sql = "insert into payment_history(sourceAccountNumber,sourceAccountName,destAccountNumber, destAccountName, amount, isSuccessful, messageSentToSourceAccount, messageSentToDestAccount) values (:sourceAccountNumber,:sourceAccountName, :destAccountNumber, :destAccountName, :amount, :isSuccessful, :messageSentToSourceAccount, :messageSentToDestAccount)";
        Map<String,Object> mp = new HashMap<>();
        mp.put("sourceAccountNumber", body.getSourceAccountNumber());
        mp.put("sourceAccountName", body.getSourceAccountName());
        mp.put("destAccountNumber", body.getDestAccountNumber());
        mp.put("destAccountName", body.getDestAccountName());
        mp.put("amount", body.getAmount());
        mp.put("isSuccessful", body.isSuccessful());
        mp.put("messageSentToSourceAccount", body.getMessageSentToSourceAccount());
        mp.put("messageSentToDestAccount", body.getMessageSentToDestAccount());

        namedParameterJdbcTemplate.update(sql,mp);


    }



}
