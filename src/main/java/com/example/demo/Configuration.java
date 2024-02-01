package com.example.demo;

import com.example.demo.services.TestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
@org.springframework.context.annotation.Configuration

public class Configuration {

    @Bean
    public TestService getTestService(){

        return new TestService();
    }
}
