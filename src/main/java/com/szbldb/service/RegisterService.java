package com.szbldb.service;

import com.szbldb.dao.UserMapper;
import com.szbldb.pojo.User;
import com.szbldb.util.JWTHelper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@Service
public class RegisterService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JavaMailSender sender;

    private static final String subject = "Please verify your account";
    private static final String content1 = "You are registering for SZBLDB.com. The captcha code is:\n";
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
            code.append(random.nextInt(0, 10));
        }
        mail.setText(content1 + code + content2);
        mail.setTo(email);
        mail.setFrom("15797079817@163.com");
        try{
            sender.send(mail);
        }catch (Exception e){
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

    public boolean signup(String username, String password, String email){
        User user = new User(username, password, email);
        return userMapper.insertUser(user) != 0;
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