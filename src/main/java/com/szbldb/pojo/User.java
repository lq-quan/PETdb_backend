package com.szbldb.pojo;

import lombok.Data;

import java.util.Random;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String email;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User() {
    }
}