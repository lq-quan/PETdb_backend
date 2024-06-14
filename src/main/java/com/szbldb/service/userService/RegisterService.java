package com.szbldb.service.userService;

import com.szbldb.dao.UserMapper;
import com.szbldb.pojo.userPojo.User;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import com.szbldb.util.JWTHelper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Service
public class RegisterService {

    private final UserMapper userMapper;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JavaMailSender sender;

    @Autowired
    public RegisterService(UserMapper userMapper, JavaMailSender sender) {
        this.userMapper = userMapper;
        this.sender = sender;
    }

    private static final String subject = "Please verify your account";
    private static final String content1 = "You are visiting resources in SZBLDB.com. The captcha code is:\n";
    private static final String content2 = "\nIt's valid in 10 minutes.If it's not operated on your own, ignore the Email. \nPlease do not Reply";
    private static final Random random = new Random();

    public boolean checkIfExisted(String username){
        return userMapper.getUserByUsername(username) == null;
    }

    public String validateEmail(String email){
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setSubject(subject);
        StringBuilder code = new StringBuilder();
        for(int i = 0; i < 6; i++){
            int next = random.nextInt(0, 36);
            if(next < 10)
                code.append(next);
            else
                code.append((char)('A' + next - 10));
        }
        mail.setText(content1 + code + content2);
        mail.setTo(email);
        mail.setFrom("15797079817@163.com");
        try{
            sender.send(mail);
        }catch (Exception e){
            log.error("邮件发送失败", e);
            return null;
        }
        System.out.println("邮件发送完毕！");
        System.out.println(code);
        return digestMD5(code.toString());
    }

    public boolean checkVerifyCode(String jwt, String code){
        String check = digestMD5(code);
        Claims claims;
        try{
            claims = JWTHelper.jwtUnpack(jwt);
        }catch (ExpiredJwtException e){
            return false;
        }
        String preCode = claims.get("code", String.class);
        return preCode.equals(check);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean signup(String username, String password, String email){
        User user = new User(username, password, email);
        userMapper.insertUser(user);
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setName(username);
        userMapper.initUserInfo(userInfo);
        return true;
    }

    public User getIdByUsername(String username){
        return userMapper.getUserByUsername(username);
    }

    public String digestMD5(String code){
        MessageDigest md;
        try{
            md = MessageDigest.getInstance("MD5");
        }catch (NoSuchAlgorithmException e){
            return null;
        }
        md.update(code.getBytes(StandardCharsets.UTF_8));
        byte[] result = md.digest();
        return new BigInteger(1, result).toString(16);
    }

}