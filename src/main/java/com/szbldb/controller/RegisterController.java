package com.szbldb.controller;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.UserPojo;
import com.szbldb.service.RegisterService;
import com.szbldb.util.JWTHelper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RegisterController {
    @Autowired
    private RegisterService registerService;

    @RequestMapping(value = "/PETdatabase/register/checkUserInfo")
    public Result checkUsername(@RequestBody UserPojo userPojo){
        String username = userPojo.getUsername(), password = userPojo.getPassword(), email = userPojo.getEmail();
        System.out.println(username + ":" + password);
        Map<String, Object> map = new HashMap<>();
        if(this.registerService.checkIfExisted(username)){
            map.put("username", username);
            map.put("password", password);
            map.put("email", email);
        }
        else
            return Result.error("The name was already used by others!");
        String code = registerService.validateEmail(email);
        if(code != null) {
            map.put("code", code);
            String jwtCode = JWTHelper.jwtPacker(map, 5);
            return Result.success(jwtCode);
            //jwtCode:包括username, password, email, 生成的code
        }
        else
            return Result.error("Failed to send code. Please check the email.");
    }

    @RequestMapping(value = "/PETdatabase/register/checkCode")
    public Result checkCode(@RequestBody UserPojo codePojo){
        String jwtCode = codePojo.getJwtCode();
        String code = codePojo.getCode();
        if(registerService.checkVerifyCode(jwtCode, code)){
            Claims claimsCode;
            try {
                claimsCode = JWTHelper.jwtUnpack(jwtCode);
            }catch (ExpiredJwtException e){
                return Result.error("The information is expired. Please retype username again.");
            }
            String username = claimsCode.get("username", String.class);
            String password = claimsCode.get("password", String.class);
            String email = claimsCode.get("email", String.class);
            if(email == null || email.isEmpty())
                return Result.error("Please verify your email address first!");
            if(registerService.signup(username, password, email)){
                return Result.success(JWTHelper.generateUserPojo(username));
            }
            else
                return Result.error("Failed to sign up. It may because someone occupied the username just now. Please try to sign up again.");
        }
        else
            return Result.error("Code is wrong or expired!");
    }
}
