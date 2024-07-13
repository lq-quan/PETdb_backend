package com.szbldb.controller.userController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import com.szbldb.pojo.userInfoPojo.UserInfoPack;
import com.szbldb.pojo.userPojo.UserPojo;
import com.szbldb.service.userService.UserInfoService;

import com.szbldb.util.JWTHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


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
    @GetMapping("/PETdatabase/user/info")
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
    @PostMapping("/PETdatabase/user/info/change")
    public Result changeInfo(@RequestHeader String token, @RequestBody UserInfoPack userInfoPack){
        userInfoService.changeInfo(token, userInfoPack.getUserInfo());
        return Result.success();
    }

    /**
     *
     * @Description 用户申请修改密码，向邮箱发送验证码
     * @param token 令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/10 16:39
     **/
    @GetMapping("/PETdatabase/user/info/checkIdentity")
    public Result checkIdentity(@RequestHeader String token){
        String jwtCode = userInfoService.checkBeforeModifyPsw(JWTHelper.getUsername(token));
        if(jwtCode == null) return Result.error("邮件发送失败", 50103);
        return Result.success(jwtCode);
    }

    /**
     *
     * @Description 检验验证码并修改密码
     * @param token 用户令牌
     * @param userPojo 包括：新密码密文，jwtCode，以及验证码
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/10 16:40
     **/
    @PostMapping("/PETdatabase/user/info/modifyPsw")
    public Result modifyPassword(@RequestHeader String token, @RequestBody UserPojo userPojo){
        String username = JWTHelper.getUsername(token);
        if(userInfoService.modifyPsw(userPojo.getJwtCode(), username, userPojo.getPassword(), userPojo.getCode())){
            return Result.success();
        }
        return Result.error("Wrong or expired code!", 50201);
    }

    /**
     *
     * @Description 获取用户邮箱
     * @param token 用户令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:52
     **/
    @GetMapping("/PETdatabase/user/info/getEmail")
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
    @GetMapping("/PETdatabase/user/info/uploadAvatar")
    public Result uploadAvatar(@RequestHeader String token, String avatar){
        try{
            String url = userInfoService.uploadAvatar(token, avatar);
            return Result.success(url);
        }catch (Exception e){
            return Result.error("failed to update avatar", 40009);
        }
    }
}
