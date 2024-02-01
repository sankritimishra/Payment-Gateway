package com.example.demo.services;

import com.example.demo.dtos.FullUserInfoDTO;
import com.example.demo.repositories.UserDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsService {

//    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
//
//    public UserDetailsService(NamedParameterJdbcTemplate namedParameterJdbcTemplate, UserDetailsRepository userDetailsRepository) {
//        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
//        this.userDetailsRepository = userDetailsRepository;
//    }

    private final UserDetailsRepository userDetailsRepository;

    public UserDetailsService(UserDetailsRepository userDetailsRepository) {
        this.userDetailsRepository = userDetailsRepository;
    }

    public FullUserInfoDTO details(String accountNumber){
        return userDetailsRepository.getUserDetailsByAccountNumber(accountNumber);
    }

    public void addNewUser(FullUserInfoDTO body) {
         userDetailsRepository.addNewUserInfo(body);
    }



    public void updateUserByAccountNumber(String accountNumber, FullUserInfoDTO body) {
        userDetailsRepository.updateUser(accountNumber,body);

    }
    @Transactional
    public void deleteUserByAccountNumber(String accountNumber) {
        userDetailsRepository.deleteUser(accountNumber);
    }
}
