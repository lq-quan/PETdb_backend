package com.szbldb.controller;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.userPojo.User;

import com.szbldb.service.LoginService;
import com.szbldb.util.JWTHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    @Autowired
    private LoginService loginService;


    @RequestMapping("/PETdatabase/user/login")
    public Result login(@RequestBody User user) {
        String username = user.getUsername();
        String password = user.getPassword();
//        System.out.println(user);
        if (loginService.check(username, password)) {
            return Result.success(JWTHelper.generateUserPojo(username));
        }
        else
            return Result.error("Invalid username or incorrect password!", 60204);
    }
}