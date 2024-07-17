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

    /**
     *
     * @Description 管理员查询用户申请信息
     * @param name 搜索关键词
     * @param status 申请状态
     * @param page （第）页数
     * @param limit 每页项数
     * @param sort 排序方式，+id / -id
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:31
     **/
    @GetMapping("/PETdatabase/dataset/license/admin/get")
    public Result searchSubmission(String name, String status, Integer page, Integer limit, String sort){
        SubmissionList submissionList = adminLicenseService.searchSubmissions(name, status, page, limit, sort);
        return Result.success(submissionList);
    }

    /**
     *
     * @Description 管理员审批用户申请
     * @param token 用户令牌
     * @param map 审批情况，包括状态，拒绝理由等
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:34
     **/
    @PostMapping("/PETdatabase/dataset/license/admin/approve")
    public Result auditSubmission(@RequestHeader String token, @RequestBody Map<String, Object> map) {
        String status = (String) map.get("status"), reason = (String)map.get("reason");
        if(!"approved".equals(status) && !"rejected".equals(status)) return Result.error("Bad request", 50008);
        Integer sid = (Integer)map.get("id");
        //System.out.println(id + " " + status);
        String username = JWTHelper.getUsername(token);
        adminLicenseService.auditSubmission(sid, username, status, reason);
        return Result.success();
    }

    /**
     *
     * @Description 管理员查询申请具体信息
     * @param id 申请 id
     * @return com.szbldb.pojo.Result
     * @author Quan Li 2024/7/4 15:35
     **/
    @GetMapping("/PETdatabase/dataset/license/admin/detail")
    public Result checkSubmissionDetail(Integer id){
        return Result.success(adminLicenseService.getSubmissionDetail(id));
    }
}
