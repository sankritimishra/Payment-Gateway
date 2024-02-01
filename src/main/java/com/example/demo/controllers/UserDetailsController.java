package com.example.demo.controllers;

import com.example.demo.dtos.FullUserInfoDTO;
import com.example.demo.services.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserDetailsController {

    public UserDetailsController(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    private final UserDetailsService userDetailsService;
    @GetMapping("/users/info/{account_number}")
    public FullUserInfoDTO getUserInfoByAccountNumber(@PathVariable("account_number") String accountNumber){
          return userDetailsService.details(accountNumber);
    }

    @PostMapping("/users/add_new_user")
    public void addNewUser(@RequestBody FullUserInfoDTO body){
           userDetailsService.addNewUser(body);
    }

    @PostMapping ("/users/update_existing_user/{account_number}")
    public void updateUserByAccountNumber(@PathVariable String account_number, @RequestBody FullUserInfoDTO body){
          userDetailsService.updateUserByAccountNumber(account_number,body);
    }

    @DeleteMapping("/users/delete_existing_user/{account_number}")
    public void deleteUserByAccountNumber(@PathVariable String account_number){
           userDetailsService.deleteUserByAccountNumber(account_number);
    }


}
