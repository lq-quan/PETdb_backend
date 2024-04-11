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

    @Override
    public String toString() {
        return "Submission{" +
                "firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", company='" + company + '\'' +
                ", degree='" + degree + '\'' +
                ", department='" + department + '\'' +
                ", contact='" + contact + '\'' +
                ", purpose='" + purpose + '\'' +
                ", address=" + address +
                ", id=" + id +
                ", status='" + status + '\'' +
                ", date=" + date +
                '}';
    }
}
