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

    /**
     *
     * @Description 发送邮件，返回验证码与用户名结合后字符串的 SHA256 值，防止被暴力破解
     * @param email 邮箱账户
     * @return java.lang.String
     * @author Quan Li 2024/7/5 16:42
     **/
    public static String sendEmail(String email, String username){
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setSubject(subject);
        StringBuilder code = new StringBuilder();
        int last = -1;
        int[] rec = new int[10];
        for(int i = 0; i < 6; i++){
            int next = random.nextInt(0, 10);
            if(next == last || rec[next] == 2)
                i--;
            else{
                code.append(next);
                rec[next]++;
                last = next;
            }
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
        System.out.println("邮件发送完毕！To: " + email);
        System.out.println(code);
        return digestSha256(code + username);
    }

    /**
     *
     * @Description 将字符串进行 SHA256 加密
     * @param code 指定字符串
     * @return java.lang.String
     * @author Quan Li 2024/7/5 16:42
     **/
    public static String digestSha256(String code){//get encoded string of code by md5
        return DigestUtils.sha256Hex(code);
    }

    /**
     *
     * @Description 解析令牌并检查验证码正确性
     * @param jwtCode 令牌
     * @param code 验证码
     * @return boolean
     * @author Quan Li 2024/7/5 16:42
     **/
    public static boolean verifyCode(String jwtCode, String code){
        String username;
        Claims claims;
        try{
            claims = JWTHelper.jwtUnpack(jwtCode);
            username = claims.get("username", String.class);
        }catch (ExpiredJwtException e){
            return false;
        }
        String preCode = claims.get("code", String.class);
        String check = digestSha256(code + username);
        return preCode.equals(check);
    }
}
