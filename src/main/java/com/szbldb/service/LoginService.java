package com.szbldb.service;

import com.szbldb.dao.UserMapper;
import com.szbldb.pojo.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class LoginService {

    @Autowired
    private UserMapper userMapper;

    private static final String jwtKey = "szbldb";

    public boolean check(String username, String password){
        User user = userMapper.getUserByUsername(username);
        if(user == null) return false;
        return user.getPassword().equals(password);
    }
}
