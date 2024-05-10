package com.szbldb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;



@EnableAsync
@SpringBootApplication
public class SzblabApplication {

    public static void main(String[] args) {
        SpringApplication.run(SzblabApplication.class, args);
    }

}
