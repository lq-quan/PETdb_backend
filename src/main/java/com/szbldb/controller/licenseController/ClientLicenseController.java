package com.szbldb.controller.licenseController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.licensePojo.Submission;
import com.szbldb.pojo.userPojo.User;
import com.szbldb.pojo.userPojo.UserPojo;
import com.szbldb.service.licenseService.ClientLicenseService;
import com.szbldb.service.userService.RegisterService;
import com.szbldb.util.JWTHelper;
import com.szbldb.util.MailHelper;
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

    /**
     * @Description 用户提交申请
     * @param submission 申请内容
     * @param token 用户令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:36
     **/
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

    /**
     *
     * @Description 用户查看自己申请状态
     * @param token 用户令牌
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:37
     **/
    @RequestMapping("/PETdatabase/dataset/license/detail")
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
    @RequestMapping("/PETdatabase/dataset/license/verifiedOrNot")
    public Result checkIfVerified(@RequestHeader String token){
        String username = JWTHelper.getUsername(token);
        Integer id = registerService.getIdByUsername(username).getId();
        if(clientLicenseService.checkIfVerified(id)){
            return Result.success("Yes");
        }
        else return Result.success("No");
    }

    /**
     *
     * @Description 往指定邮箱发送验证码
     * @param user 获取用户邮箱，以便发送验证码
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:39
     **/
    @RequestMapping("/PETdatabase/dataset/license/verifyEmail")
    public Result verifyEmail(@RequestBody User user){
        String email = user.getEmail();
        if(!StringUtils.hasLength(email)){
            throw new RuntimeException("No email found");
        }
        Map<String, Object> map = new HashMap<>();
        String code = MailHelper.sendEmail(email);
        if(code == null){
            return Result.error("邮件发送失败", 50103);
        }
        map.put("code", code);
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
    @RequestMapping("/PETdatabase/dataset/license/verifyCode")
    public Result verifyCode(@RequestBody UserPojo userPojo, @RequestHeader String token){
        String jwtCode = userPojo.getJwtCode();
        String code = userPojo.getCode();
        String username = JWTHelper.getUsername(token);
        if(MailHelper.verifyCode(jwtCode, code)){
            User user = registerService.getIdByUsername(username);
            clientLicenseService.insertValidEmail(user.getId(), user.getEmail());
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
    @RequestMapping("/PETdatabase/dataset/license/status")
    public Result checkStatus(@RequestHeader String token){
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
