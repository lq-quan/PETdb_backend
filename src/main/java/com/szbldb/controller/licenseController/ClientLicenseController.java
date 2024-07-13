package com.szbldb.controller.licenseController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.licensePojo.Submission;
import com.szbldb.pojo.userPojo.UserPojo;
import com.szbldb.service.licenseService.ClientLicenseService;
import com.szbldb.util.JWTHelper;
import com.szbldb.util.MailHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ClientLicenseController {

    private final ClientLicenseService clientLicenseService;


    @Autowired
    public ClientLicenseController(ClientLicenseService clientLicenseService) {
        this.clientLicenseService = clientLicenseService;
    }

    /**
     * @Description 用户提交申请
     * @param submission 申请内容
     * @param token 用户令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:36
     **/
    @PostMapping("/PETdatabase/dataset/license/submit")
    public Result applicationSubmit(@RequestBody Submission submission, @RequestHeader String token){
        //System.out.println(submission);
        String username = JWTHelper.getUsername(token);
        if(!clientLicenseService.checkIfVerified(username)){
            return Result.error("Email validation failed or expired!", 52004);
        }
        if(clientLicenseService.createApplication(submission, username))
            return Result.success();
        else return Result.error("Application already exists!", 52003);
    }

    /**
     *
     * @Description 用户查看自己申请状态
     * @param token 用户令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:37
     **/
    @GetMapping("/PETdatabase/dataset/license/detail")
    public Result checkSubmission(@RequestHeader String token){
        String username = JWTHelper.getUsername(token);
        Submission submission = clientLicenseService.checkApplicationByUsername(username);
        return Result.success(submission);
    }

    /**
     *
     * @Description 检查用户是否通过邮箱申请
     * @param token 用户令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:38
     **/
    @GetMapping("/PETdatabase/dataset/license/verifiedOrNot")
    public Result checkIfVerified(@RequestHeader String token){
        String username = JWTHelper.getUsername(token);
        if(clientLicenseService.checkIfVerified(username)){
            return Result.success("Yes");
        }
        else return Result.success("No");
    }

    /**
     *
     * @Description 往指定邮箱发送验证码
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:39
     **/
    @PostMapping("/PETdatabase/dataset/license/verifyEmail")
    public Result verifyEmail(@RequestHeader String token){
        String username = JWTHelper.getUsername(token);
        String email = clientLicenseService.getEmail(username);
        Map<String, Object> map = new HashMap<>();
        String code = MailHelper.sendEmail(email, username);
        if(code == null){
            return Result.error("邮件发送失败", 50103);
        }
        map.put("code", code);
        map.put("username", username);
        String jwtCode = JWTHelper.jwtPacker(map, 10);
        return Result.success(jwtCode);
    }

    /**
     *
     * @Description 检查验证码
     * @param userPojo 验证码信息
     * @param token 用户令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:41
     **/
    @PostMapping("/PETdatabase/dataset/license/verifyCode")
    public Result verifyCode(@RequestBody UserPojo userPojo, @RequestHeader String token){
        String jwtCode = userPojo.getJwtCode();
        if(jwtCode == null || jwtCode.isEmpty()) return Result.error("Please send the email before next", 50203);
        String code = userPojo.getCode();
        String username = JWTHelper.getUsername(token);
        String usernameInJwt = JWTHelper.getUsername(jwtCode);
        if(username == null || !username.equals(usernameInJwt)) return Result.error("身份验证失败！", 50203);
        if(MailHelper.verifyCode(jwtCode, code)){
            clientLicenseService.insertValidEmail(username);
            return Result.success(true);
        }
        else return Result.error("Wrong or Expired code!", 50201);
    }

    /**
     *
     * @Description 检查申请状态
     * @param token 用户令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:42
     **/
    @GetMapping("/PETdatabase/dataset/license/status")
    public Result checkStatus(@RequestHeader(required = false) String token){
        if(token == null){
            Submission submission = new Submission();
            submission.setStatus("not_login");
            return Result.success(submission);
        }
        String username = JWTHelper.getUsername(token);
        Submission submission = clientLicenseService.getStatusByUsername(username);
        return Result.success(submission);
    }

    /**
     *
     * @Description 用户更新申请信息
     * @param submission 需要更新的数据
     * @param token 用户令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:42
     **/
    @PostMapping("/PETdatabase/dataset/license/update")
    public Result updateApplication(@RequestBody Submission submission, @RequestHeader String token){
        //System.out.println(submission);
        String username = JWTHelper.getUsername(token);
        if(!clientLicenseService.checkIfVerified(username)){
            return Result.error("Email validation failed or expired!", 52004);
        }
        if(clientLicenseService.updateApplication(submission, username))
            return Result.success();
        else return Result.error("No submission found!", 52005);
    }
}
