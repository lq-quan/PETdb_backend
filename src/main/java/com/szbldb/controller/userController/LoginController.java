package com.szbldb.controller.userController;

import com.szbldb.exception.UserException;
import com.szbldb.pojo.Result;
import com.szbldb.pojo.userPojo.User;

import com.szbldb.pojo.userPojo.UserPojo;
import com.szbldb.service.userService.LoginService;
import com.szbldb.util.JWTHelper;
import com.szbldb.util.MailHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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
        try{
            if (loginService.check(username, password)) {
                return Result.success(JWTHelper.generateUserPojo(username));
            }
            else
                return Result.error("Invalid username or incorrect password!", 60204);
        }catch (UserException e){
            log.info("检测到管理员登录：" + username, e);
            String code = MailHelper.sendEmail(loginService.getEmail(username));
            if(code != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", code);
                String jwtCode = JWTHelper.jwtPacker(map, 10);
                return Result.error(jwtCode, 50005);
                //jwtCode:包括生成的code
            }
            else {
                log.error("出现异常！邮件发送失败");
                return Result.error("Failed to send code.");
            }
        }
    }

    @RequestMapping("/PETdatabase/user/login/checkAdmin")
    public Result checkAdminLogin(@RequestBody UserPojo codePojo){
        String jwtCode = codePojo.getJwtCode();
        String code = codePojo.getCode();
        if(MailHelper.verifyCode(jwtCode, code)) {
            return Result.success(JWTHelper.generateUserPojo(codePojo.getUsername()));
        }
        log.warn("管理员输入错误验证码：" + codePojo.getUsername());
        return Result.error("Wrong or Expired verify code!", 50202);
    }

    @RequestMapping("/PETdatabase/user/logout")
    public Result logout(@RequestHeader String token){
        loginService.logout(token);
        return Result.success(true);
    }
}