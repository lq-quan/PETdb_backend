package com.szbldb.controller.userController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import com.szbldb.pojo.userInfoPojo.UserInfoPack;
import com.szbldb.service.userService.UserInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




@RestController
public class UserInfoController {

    private final UserInfoService userInfoService;


    public UserInfoController(@Autowired UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @RequestMapping("/PETdatabase/user/info")
    public Result getInfo(@RequestHeader String token){
        //System.out.println(token);
        UserInfo userInfo = userInfoService.getUserInfo(token);
        if(userInfo == null)
            return Result.error("Not log in yet!", 50008);
        return Result.success(userInfo);
    }

    @RequestMapping("/PETdatabase/user/info/change")
    public Result changeInfo(@RequestHeader String token, @RequestBody UserInfoPack userInfoPack){
        userInfoService.changeInfo(token, userInfoPack.getUserInfo());
        return Result.success();
    }

    @RequestMapping("/PETdatabase/user/info/getEmail")
    public Result getEmail(@RequestHeader String token){
        return Result.success(userInfoService.getEmail(token));
    }

    @RequestMapping("/PETdatabase/user/info/uploadAvatar")
    public Result uploadAvatar(@RequestHeader String token, String avatar){
        try{
            String url = userInfoService.uploadAvatar(token, avatar);
            return Result.success(url);
        }catch (Exception e){
            return Result.error("failed to update avatar", 40009);
        }
    }
}
