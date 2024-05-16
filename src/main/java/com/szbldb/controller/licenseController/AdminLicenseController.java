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
    public Result searchSubmission(String name, String status, Integer page, Integer limit){
        SubmissionList submissionList = adminLicenseService.searchSubmissions(name, status, page, limit);
        return Result.success(submissionList);
    }

    @PostMapping("/PETdatabase/dataset/license/admin/approve")
    public Result auditSubmission(@RequestHeader String token, @RequestBody Map<String, Object> map) {
        String status = (String) map.get("status"), reason = (String)map.get("reason");
        Integer sid = (Integer)map.get("id");
        //System.out.println(id + " " + status);
        String username = JWTHelper.getUsername(token);
        adminLicenseService.auditSubmission(sid, username, status, reason);
        return Result.success();
    }

    @RequestMapping("/PETdatabase/dataset/license/admin/detail")
    public Result checkSubmissionDetail(Integer id){
        return Result.success(adminLicenseService.getSubmissionDetail(id));
    }
}
