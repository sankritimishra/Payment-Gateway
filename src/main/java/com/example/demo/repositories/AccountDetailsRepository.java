package com.example.demo.repositories;

import com.example.demo.dtos.AccountDetailsDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class AccountDetailsRepository {

    public AccountDetailsRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public String getAccountBalance(String accountNumber){
        String sql = "select * from user_account_details where account_number = :accountNumber;";
        Map<String,String> mp = new HashMap<>();
        mp.put("accountNumber",accountNumber);
        var accountDetails = namedParameterJdbcTemplate.queryForList(sql,mp).get(0);
        //return new AccountDetailsDTO(((Integer) accountDetails.get("id")), accountDetails.get("balance").toString(), accountDetails.get("account_number").toString());
         return accountDetails.get("balance").toString();
    }

    public void updateAccountBalance(String accountNumber, AccountDetailsDTO body) {
        String sql = "update user_account_details set balance = :balance where account_number= :accountNumber;";
        Map<String, String> mp = new HashMap<>();
        mp.put("balance",body.getBalance());
        mp.put("accountNumber", accountNumber);

        namedParameterJdbcTemplate.update(sql,mp);
    }


    public void addDetailsByAccountNumber(String accountNumber, AccountDetailsDTO body) {
        String sql = "insert into user_account_details(balance,account_number) values(:balance, :accountNumber);";

        Map<String,Object>mp = new HashMap<>();
        mp.put("balance", body.getBalance());
        mp.put("accountNumber", body.getAccountNumber());

        namedParameterJdbcTemplate.update(sql,mp);
    }
}
