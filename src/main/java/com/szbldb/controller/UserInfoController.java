package com.szbldb.controller;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.UserPojo;
import com.szbldb.pojo.UserToken;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import com.szbldb.service.UserInfoService;
import com.szbldb.util.JWTHelper;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/PETdatabase/user/info")
    public Result getInfo(String token){
        System.out.println(token);
        UserInfo userInfo = userInfoService.getUserInfo(token);
        if(userInfo == null)
            return Result.error("Not log in yet!", 50008);
        return Result.success(userInfo);
    }

    @RequestMapping("/PETdatabase/user/info/change")
    public Result createInfo(String token, @RequestBody UserInfo userInfo){
        userInfoService.changeInfo(token, userInfo);
        return Result.success();
    }
}
