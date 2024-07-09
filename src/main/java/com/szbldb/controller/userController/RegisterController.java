package com.szbldb.controller.userController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.userPojo.UserPojo;
import com.szbldb.service.userService.RegisterService;
import com.szbldb.util.JWTHelper;
import com.szbldb.util.MailHelper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RegisterController {

    private final RegisterService registerService;

    public RegisterController(@Autowired RegisterService registerService) {
        this.registerService = registerService;
    }

    /**
     *
     * @Description 检查注册用户名，并发送邮件验证码
     * @param userPojo 用户注册信息
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:48
     **/
    @PostMapping(value = "/PETdatabase/register/checkUserInfo")
    public Result checkUsername(@RequestBody UserPojo userPojo){
        String username = userPojo.getUsername(), password = userPojo.getPassword(), email = userPojo.getEmail();
        System.out.println(username + ":" + password);
        Map<String, Object> map = new HashMap<>();
        if(this.registerService.checkIfExisted(username)){
            password = BCrypt.hashpw(password, BCrypt.gensalt());
            map.put("username", username);
            map.put("password", password);
            map.put("email", email);
        }
        else
            return Result.error("The name is too long or has been used by others!", 50101);
        String code = MailHelper.sendEmail(email, username);
        if(code != null) {
            map.put("code", code);
            String jwtCode = JWTHelper.jwtPacker(map, 10);
            return Result.success(jwtCode);
            //jwtCode:包括username, password密文, email, 生成的code
        }
        else
            return Result.error("Failed to send code. Please check the email.");
    }

    /**
     *
     * @Description 检查验证码，并生成令牌
     * @param codePojo 验证码信息
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:49
     **/
    @PostMapping(value = "/PETdatabase/register/checkCode")
    public Result checkCode(@RequestBody UserPojo codePojo){
        String jwtCode = codePojo.getJwtCode();
        String code = codePojo.getCode();
        if(MailHelper.verifyCode(jwtCode, code)){
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
