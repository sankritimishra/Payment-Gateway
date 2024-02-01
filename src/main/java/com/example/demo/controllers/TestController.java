package com.example.demo.controllers;

import com.example.demo.services.TestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    public TestController(TestService service) {
        this.service = service;
    }
    @Value("${value.from.file}")
    private String value;
    private final TestService service;
    @GetMapping("/test")
    public String getTestResult() {
        System.out.println(value);
        return service.getTest();
        //return "Test works!";
    }
}


