package com.example.demo.repositories;

import com.example.demo.dtos.AccountDetailsDTO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AccountDetailsRepository {

    public AccountDetailsRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public BigDecimal getAccountBalance(String accountNumber) {
        String sql = "SELECT balance FROM user_account_details WHERE account_number = :accountNumber";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountNumber", accountNumber);

        List<BigDecimal> result = namedParameterJdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> rs.getBigDecimal("balance")
        );

        return result.isEmpty() ? null : result.get(0);
    }

    public boolean accountExists(String accountNumber) {
        String sql = "SELECT COUNT(*) FROM user_account_details WHERE account_number = :accountNumber";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountNumber", accountNumber);

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
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
