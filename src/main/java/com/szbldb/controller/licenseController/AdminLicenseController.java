package com.szbldb.controller.licenseController;

import com.szbldb.pojo.Result;
import com.szbldb.pojo.licensePojo.SubmissionList;
import com.szbldb.service.licenseService.AdminLicenseService;
import com.szbldb.util.JWTHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AdminLicenseController {

    private final AdminLicenseService adminLicenseService;

    public AdminLicenseController(@Autowired AdminLicenseService adminLicenseService) {
        this.adminLicenseService = adminLicenseService;
    }

    @RequestMapping("/PETdatabase/dataset/license/admin/get")
    public Result searchSubmission(String word, Integer page, Integer limit){
        SubmissionList submissionList = adminLicenseService.searchSubmissions(word, page, limit);
        return Result.success(submissionList);
    }

    @PostMapping("/PETdatabase/dataset/license/admin/approve")
    public Result auditSubmission(@RequestHeader String token, @RequestBody Map<String, Object> map) {
        String status = (String) map.get("status"), reason = (String)map.get("reason");
        Integer id = (Integer)map.get("id");
        //System.out.println(id + " " + status);
        String username = JWTHelper.getUsername(token);
        adminLicenseService.auditSubmission(id, username, status, reason);
        return Result.success();
    }
}
