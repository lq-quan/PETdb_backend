package com.szbldb.controller.userController;

import com.szbldb.exception.UserException;
import com.szbldb.pojo.Result;
import com.szbldb.pojo.userPojo.User;

import com.szbldb.pojo.userPojo.UserPojo;
import com.szbldb.service.userService.LoginService;
import com.szbldb.util.JWTHelper;
import com.szbldb.util.MailHelper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(@Autowired LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     *
     * @Description 用户登录
     * @param user 用户名及密码
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:45
     **/
    @PostMapping("/PETdatabase/user/login")
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
            log.info("检测到管理员登录：" + username);
            //String code = MailHelper.sendEmail(loginService.getEmail(username));
            String code = DigestUtils.sha256Hex("123456" + username);
            if(code != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", code);
                map.put("username", username);
                String jwtCode = JWTHelper.jwtPacker(map, 10);
                return Result.error(jwtCode, 50005);
                //jwtCode:包括生成的code, username
            }
            else {
                log.error("出现异常！邮件发送失败");
                return Result.error("Failed to send code.");
            }
        }
    }

    /**
     *
     * @Description 检查管理员登录
     * @param codePojo 验证码信息
     * @param request 请求信息，用于获取 IP 地址实现单点登录
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:46
     **/
    @PostMapping("/PETdatabase/user/login/checkAdmin")
    public Result checkAdminLogin(@RequestBody UserPojo codePojo, HttpServletRequest request){
        String jwtCode = codePojo.getJwtCode();
        String code = codePojo.getCode();
        String ipAddress = request.getRemoteAddr();
        String username;
        try{
            username = JWTHelper.jwtUnpack(jwtCode).get("username", String.class);
        }catch (ExpiredJwtException ee){
            return Result.error("Wrong or Expired verify code!", 50202);
        }
        if(MailHelper.verifyCode(jwtCode, code)) {
            UserPojo userPojo = JWTHelper.generateUserPojo(username);
            loginService.updateAdmin(username, ipAddress, userPojo.getJwtUser());
            log.info("管理员 " + username + " 成功登录，IP：" + ipAddress);
            return Result.success(userPojo);
        }
        log.warn("管理员输入错误验证码：" + codePojo.getUsername());
        return Result.error("Wrong or Expired verify code!", 50202);
    }

    /**
     *
     * @Description 用户登出
     * @param token 用户令牌，用于将其废除
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:47
     **/
    @PostMapping("/PETdatabase/user/logout")
    public Result logout(@RequestHeader String token){
        loginService.logout(token);
        return Result.success(true);
    }
}