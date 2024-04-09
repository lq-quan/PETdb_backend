package com.szbldb.pojo.licensePojo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Submission {
    private String firstname;
    private String lastname;
    private String company;
    private String degree;
    private String department;
    private String contact;
    private String purpose;
    private Address address;

    private Integer id;
    private String status;
    private LocalDate date;
}
