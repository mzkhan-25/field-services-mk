package com.fieldservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FieldServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(FieldServicesApplication.class, args);
    }
}
