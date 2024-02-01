package com.example.demo.repositories;

import com.example.demo.dtos.FullUserInfoDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserDetailsRepository {

    public UserDetailsRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public FullUserInfoDTO getUserDetailsByAccountNumber(String accountNumber){
        String sql = "select * from users where account_number = :accountNumber;";
        Map<String,String> mp = new HashMap<>();
        mp.put("accountNumber",accountNumber);
        var userDetails = namedParameterJdbcTemplate.queryForList(sql, mp).get(0);
        return new FullUserInfoDTO(userDetails.get("account_number").toString(), userDetails.get("username").toString(), userDetails.get("first_name").toString(), userDetails.get("middle_name").toString(), userDetails.get("last_name").toString(), ((Integer) userDetails.get("age")), userDetails.get("location").toString());
    }

    public void addNewUserInfo(FullUserInfoDTO body){
        String sql;
        sql = "insert into users (account_number,username,first_name,middle_name,last_name,age,location) values (:accountNumber,:username,:firstName,:middleName,:lastName,:age,:location);";
        Map<String,Object>mp = new HashMap<>();
        mp.put("accountNumber", body.getAccountNumber());
        mp.put("username", body.getUsername());
        mp.put("firstName", body.getFirstName());
        mp.put("middleName", body.getMiddleName());
        mp.put("lastName", body.getLastName());
        mp.put("age", body.getAge());
        mp.put("location", body.getLocation());

        namedParameterJdbcTemplate.update(sql,mp);

    }


    public void deleteUser(String accountNumber) {
        String sql = "delete from users where account_number = :accountNo;";
        Map<String,String> mp = new HashMap<>();
        mp.put("accountNo", accountNumber);
//        namedParameterJdbcTemplate.getJdbcTemplate().update(sql);
        namedParameterJdbcTemplate.update(sql,mp);



    }

    public void updateUser(String accountNumber, FullUserInfoDTO body) {
        String sql = "update users set account_number = :accountNumber, username = :username, first_name = :firstName, middle_name = :middleName, last_name=:lastName, age=:age,location = :location where account_number = :accountNumber ;";
        Map<String,Object>mp = new HashMap<>();
        mp.put("accountNumber", accountNumber);
        mp.put("username", body.getUsername());
        mp.put("firstName", body.getFirstName());
        mp.put("middleName", body.getMiddleName());
        mp.put("lastName", body.getLastName());
        mp.put("age", body.getAge());
        mp.put("location", body.getLocation());

        namedParameterJdbcTemplate.update(sql,mp);
    }
}
