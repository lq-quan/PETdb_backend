package com.szbldb.controller;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import com.szbldb.pojo.userInfoPojo.UserInfoPack;
import com.szbldb.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/PETdatabase/user/info")
    public Result getInfo(@RequestHeader String token){
        //System.out.println(token);
        UserInfo userInfo = userInfoService.getUserInfo(token);
        if(userInfo == null)
            return Result.error("Not log in yet!", 50008);
        return Result.success(userInfo);
    }

    @RequestMapping("/PETdatabase/user/info/change")
    public Result createInfo(@RequestHeader String token, @RequestBody UserInfoPack userInfoPack){
        UserInfo userInfo = userInfoPack.getUserInfo();
//        System.out.println(userInfo.toString());
        userInfoService.changeInfo(token, userInfo);
        return Result.success();
    }
}
