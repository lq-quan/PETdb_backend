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

    /**
     *
     * @Description 获取用户信息
     * @param token 用户令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:51
     **/
    @RequestMapping("/PETdatabase/user/info")
    public Result getInfo(@RequestHeader String token){
        //System.out.println(token);
        UserInfo userInfo = userInfoService.getUserInfo(token);
        if(userInfo == null)
            return Result.error("Not log in yet!", 50008);
        return Result.success(userInfo);
    }

    /**
     *
     * @Description 用户修改信息
     * @param token 用户令牌
     * @param userInfoPack 需要更新的信息
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:51
     **/
    @RequestMapping("/PETdatabase/user/info/change")
    public Result changeInfo(@RequestHeader String token, @RequestBody UserInfoPack userInfoPack){
        userInfoService.changeInfo(token, userInfoPack.getUserInfo());
        return Result.success();
    }

    /**
     *
     * @Description 获取用户邮箱
     * @param token 用户令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:52
     **/
    @RequestMapping("/PETdatabase/user/info/getEmail")
    public Result getEmail(@RequestHeader String token){
        return Result.success(userInfoService.getEmail(token));
    }

    /**
     * @Description 用户上传头像，返回上传地址
     * @param token 用户令牌
     * @param avatar 头像文件名
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:53
     **/
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
