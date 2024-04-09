package com.szbldb.controller.licenseController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.licensePojo.Submission;
import com.szbldb.service.licenseService.ClientLicenseService;
import com.szbldb.util.JWTHelper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientLicenseController {
    @Autowired
    private ClientLicenseService clientLicenseService;

    @RequestMapping("/PETdatabase/dataset/license/submit")
    public Result applicationSubmit(@RequestBody Submission submission, @RequestHeader String token){
        Claims claims;
        try{
            claims = JWTHelper.jwtUnpack(token);
        }catch (ExpiredJwtException e){
            return Result.error("Not_Login", 50007);
        }
        String username = claims.get("username", String.class);
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
            Claims claims;
            try{
                claims = JWTHelper.jwtUnpack(token);
            }catch (ExpiredJwtException e){
                return Result.error("Not_Login", 50007);
            }
            String username = claims.get("username", String.class);
            Submission submission = clientLicenseService.checkApplicationByUsername(username);
            return Result.success(submission);
        }
    }
}
