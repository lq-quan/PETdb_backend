package com.szbldb.controller.licenseController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.licensePojo.Submission;
import com.szbldb.pojo.userPojo.User;
import com.szbldb.pojo.userPojo.UserPojo;
import com.szbldb.service.licenseService.ClientLicenseService;
import com.szbldb.service.userService.RegisterService;
import com.szbldb.util.JWTHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ClientLicenseController {

    private final ClientLicenseService clientLicenseService;

    private final RegisterService registerService;

    @Autowired
    public ClientLicenseController(ClientLicenseService clientLicenseService, RegisterService registerService) {
        this.clientLicenseService = clientLicenseService;
        this.registerService = registerService;
    }

    @RequestMapping("/PETdatabase/dataset/license/submit")
    public Result applicationSubmit(@RequestBody Submission submission, @RequestHeader String token){
        //System.out.println(submission);
        String username = JWTHelper.getUsername(token);
        Integer id = registerService.getIdByUsername(username).getId();
        if(!clientLicenseService.checkIfVerified(id)){
            return Result.error("Email validation failed or expired!", 52004);
        }
        if(clientLicenseService.createApplication(submission, username))
            return Result.success();
        else return Result.error("Application already exists!", 52003);
    }

    @RequestMapping("/PETdatabase/dataset/license/detail")
    public Result checkSubmission(@RequestHeader String token){
        String username = JWTHelper.getUsername(token);
        Submission submission = clientLicenseService.checkApplicationByUsername(username);
        return Result.success(submission);
    }

    @RequestMapping("/PETdatabase/dataset/license/verifiedOrNot")
    public Result checkIfVerified(@RequestHeader String token){
        String username = JWTHelper.getUsername(token);
        Integer id = registerService.getIdByUsername(username).getId();
        if(clientLicenseService.checkIfVerified(id)){
            return Result.success("Yes");
        }
        else return Result.success("No");
    }

    @RequestMapping("/PETdatabase/dataset/license/verifyEmail")
    public Result verifyEmail(@RequestBody User user){
        String email = user.getEmail();
        if(!StringUtils.hasLength(email)){
            throw new RuntimeException("No email found");
        }
        Map<String, Object> map = new HashMap<>();
        String code = registerService.validateEmail(email);
        if(code == null){
            return Result.error("邮件发送失败", 50103);
        }
        map.put("code", code);
        String jwtCode = JWTHelper.jwtPacker(map, 10);
        return Result.success(jwtCode);
    }

    @RequestMapping("/PETdatabase/dataset/license/verifyCode")
    public Result verifyCode(@RequestBody UserPojo userPojo, @RequestHeader String token){
        String jwtCode = userPojo.getJwtCode();
        String code = userPojo.getCode();
        String username = JWTHelper.getUsername(token);
        if(registerService.checkVerifyCode(jwtCode, code)){
            User user = registerService.getIdByUsername(username);
            clientLicenseService.insertValidEmail(user.getId(), user.getEmail());
            return Result.success(true);
        }
        else return Result.error("Wrong or Expired code!", 50201);
    }

    @RequestMapping("/PETdatabase/dataset/license/status")
    public Result checkStatus(@RequestHeader String token){
        String username = JWTHelper.getUsername(token);
        Submission submission = clientLicenseService.getStatusByUsername(username);
        return Result.success(submission);
    }

    @RequestMapping("/PETdatabase/dataset/license/update")
    public Result updateApplication(@RequestBody Submission submission, @RequestHeader String token){
        //System.out.println(submission);
        String username = JWTHelper.getUsername(token);
        Integer id = registerService.getIdByUsername(username).getId();
        if(!clientLicenseService.checkIfVerified(id)){
            return Result.error("Email validation failed or expired!", 52004);
        }
        if(clientLicenseService.updateApplication(submission, username))
            return Result.success();
        else return Result.error("No submission found!", 52005);
    }
}
