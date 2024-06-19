package com.szbldb.controller.userController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.userPojo.User;

import com.szbldb.service.userService.LoginService;
import com.szbldb.util.JWTHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(@Autowired LoginService loginService) {
        this.loginService = loginService;
    }

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

    @RequestMapping("/PETdatabase/user/logout")
    public Result logout(@RequestHeader String token){
        loginService.logout(token);
        return Result.success(true);
    }
}