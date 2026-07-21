package com.example.demo.repositories;

import com.example.demo.dtos.AccountDetailsDTO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * Same as getAccountBalance but takes an exclusive row lock (SELECT ... FOR UPDATE).
     * Must be called within a transaction. Blocks any other transaction from reading-for-update
     * or writing this row until the current transaction commits/rolls back, closing the
     * read-then-write race that a plain SELECT followed by UPDATE would allow.
     */
    public BigDecimal getAccountBalanceForUpdate(String accountNumber) {
        String sql = "SELECT balance FROM user_account_details WHERE account_number = :accountNumber FOR UPDATE";

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
        String sql = "UPDATE user_account_details SET balance = :balance WHERE account_number = :accountNumber";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("balance", body.getBalance())
                .addValue("accountNumber", accountNumber);

        namedParameterJdbcTemplate.update(sql, params);
    }


    public void addDetailsByAccountNumber(String accountNumber, AccountDetailsDTO body) {
        String sql = "INSERT INTO user_account_details (balance, account_number) VALUES (:balance, :accountNumber)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("balance", body.getBalance())
                .addValue("accountNumber", body.getAccountNumber());

        namedParameterJdbcTemplate.update(sql, params);
    }
}
