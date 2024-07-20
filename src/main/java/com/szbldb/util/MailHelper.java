package com.szbldb.util;

import com.szbldb.exception.LicenseException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${MAIL_FROM}")
    private String initFrom;

    private static String from;
    private JavaMailSender initSender;

    @Autowired
    public void setInitSender(JavaMailSender initSender){
        this.initSender = initSender;
    }

    @PostConstruct
    public void init(){
        mailSender = initSender;
        from = initFrom;
    }

    /**
     *
     * @Description 发送邮件，返回验证码与用户名结合后字符串的 SHA256 值，防止被暴力破解；发送失败，返回 null
     * @param email 邮箱账户
     * @return java.lang.String
     * @author Quan Li 2024/7/5 16:42
     **/
    public static String sendEmail(String email, String username){
        //String code = "123456";
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
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setSubject(subject);
        mail.setText(content1 + code + content2);
        mail.setTo(email);
        mail.setFrom(from);
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
     * @Description 管理员审批申请，向申请人发送邮件
     * @param email 申请人邮件
     * @param result 审批结果，approved / rejected
     * @author Quan Li 2024/7/15 16:01
     **/
    public static void sendAuditResult(String email, String result){
        SimpleMailMessage mail = new SimpleMailMessage();
        String content;
        if ("approved".equals(result)) {
            content = "Your application was APPROVED. Now you can access our public data with ease! ";
        }
        else if("rejected".equals(result)){
            content = "Your application was REJECTED. Please modify your application and submit again. ";
        }
        else throw new LicenseException("Invalid string! ");
        mail.setSubject("Result of your data application");
        mail.setText(content);
        mail.setTo(email);
        mail.setFrom(from);
        try{
            mailSender.send(mail);
        }catch (Exception e){
            log.error("邮件发送失败", e);
            throw new LicenseException("failed to send the email");
        }
    }

    public static void informReceiveApplication(){
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setSubject("Application Received");
        mail.setText("The PETdatabase just received an application. Please check the website.");
        mail.setTo(from);
        mail.setFrom(from);
        try{
            mailSender.send(mail);
        }catch (Exception e){
            log.error("邮件发送失败", e);
            throw new LicenseException("failed to send the email");
        }
    }

    /**
     *
     * @Description 将字符串进行 SHA256 加密
     * @param code 指定字符串
     * @return java.lang.String
     * @author Quan Li 2024/7/5 16:42
     **/
    public static String digestSha256(String code){//get encoded string of code by sha256
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
