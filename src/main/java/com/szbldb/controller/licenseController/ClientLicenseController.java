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
        clientLicenseService.createApplication(submission, username);
        return Result.success();
    }

    @RequestMapping("/PETdatabase/dataset/license/detail")
    public Result checkSubmission(@RequestHeader String token, Integer id){
        if(id != null){
            Submission submission = clientLicenseService.checkApplicationBySid(id);
            return Result.success(submission);
        }
        else{
            String username = JWTHelper.getUsername(token);
            Submission submission = clientLicenseService.checkApplicationByUsername(username);
            return Result.success(submission);
        }
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
            throw new RuntimeException("Fail to send code!");
        }
        map.put("code", code);
        String jwtCode = JWTHelper.jwtPacker(map, 10);
        return Result.success(jwtCode);
    }

    @RequestMapping("/PETdatabase/dataset/license/verifyCode")
    public Result verifyCode(@RequestBody UserPojo userPojo){
        String jwtCode = userPojo.getJwtCode();
        String code = userPojo.getCode();
        if(registerService.checkVerifyCode(jwtCode, code)){
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
}
