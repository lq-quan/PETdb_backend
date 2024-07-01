package com.szbldb.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
public class MailHelper {

    private static JavaMailSender mailSender;

    private static final String subject = "Please verify your account";
    private static final String content1 = "You are visiting resources in SZBLDB.com. The captcha code is:\n";
    private static final String content2 = "\nIt's valid in 10 minutes.If it's not operated on your own, ignore the Email. \nPlease do not Reply";
    private static final Random random = new Random();
    private JavaMailSender initSender;

    @Autowired
    public void setInitSender(JavaMailSender initSender){
        this.initSender = initSender;
    }

    @PostConstruct
    public void init(){
        mailSender = initSender;
    }

    public static String sendEmail(String email){
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
            mailSender.send(mail);
        }catch (Exception e){
            log.error("邮件发送失败", e);
            return null;
        }
        System.out.println("邮件发送完毕！");
        System.out.println(code);
        return digestMD5(code.toString());
    }

    public static String digestMD5(String code){//get encoded string of code by md5
        return DigestUtils.md5Hex(code);
    }

    public static boolean verifyCode(String jwtCode, String code){
        String check = digestMD5(code);
        Claims claims;
        try{
            claims = JWTHelper.jwtUnpack(jwtCode);
        }catch (ExpiredJwtException e){
            return false;
        }
        String preCode = claims.get("code", String.class);
        return preCode.equals(check);
    }
}
