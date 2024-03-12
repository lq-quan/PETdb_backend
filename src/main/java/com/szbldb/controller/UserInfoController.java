package com.szbldb.controller;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.UserPojo;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import com.szbldb.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/PETdatabase/user/info")
    public Result getInfo(UserPojo userPojo){
        String jwtUser = userPojo.getJwtUser();
        System.out.println(jwtUser);
        UserInfo userInfo = userInfoService.getUserInfo(jwtUser);
        if(userInfo == null)
            return Result.error("Not log in yet!", 50008);
        return Result.success(userInfo);
    }

//    @RequestMapping("/PETdatabase/user/info")
//    public Result createInfo(@RequestBody UserInfo userInfo){
//
//    }
}
