package com.szbldb.service.licenseService;

import com.szbldb.dao.LicenseMapper;
import com.szbldb.pojo.licensePojo.Submission;
import com.szbldb.pojo.licensePojo.SubmissionList;
import com.szbldb.service.logService.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminLicenseService {

    private final LicenseMapper licenseMapper;

    private final LogService logService;

    @Autowired
    public AdminLicenseService(LicenseMapper licenseMapper, LogService logService) {
        this.licenseMapper = licenseMapper;
        this.logService = logService;
    }

    /**
     *
     * @Description 通过指定信息搜索申请列表
     * @param name 申请用户名称
     * @param status 申请状态
     * @param page （第）页数
     * @param limit 每页项数
     * @return com.szbldb.pojo.licensePojo.SubmissionList
     * @author Quan Li 2024/7/5 15:55
     **/
    public SubmissionList searchSubmissions(String name, String status, Integer page, Integer limit){
        List<Submission> list = licenseMapper.searchSubmissions(name, status, (page - 1) * limit, limit);
        SubmissionList submissionList = new SubmissionList();
        submissionList.setItems(list);
        submissionList.setTotal(list.size());
        return submissionList;
    }

    /**
     *
     * @Description 获取指定申请具体信息
     * @param sid 申请 id
     * @return com.szbldb.pojo.licensePojo.Submission
     * @author Quan Li 2024/7/5 16:01
     **/
    public Submission getSubmissionDetail(Integer sid){
        return licenseMapper.checkApplication(sid);
    }

    /**
     *
     * @Description 审批指定申请
     * @param id 申请 id
     * @param auditor 审批员
     * @param status 审批结果（通过/不通过）
     * @param reason 理由
     * @author Quan Li 2024/7/5 16:03
     **/
    @Transactional(rollbackFor = Exception.class)
    public void auditSubmission(Integer id, String auditor, String status, String reason){
        licenseMapper.updateSubmissionById(id, auditor, status, reason);
        logService.addLog("对下载许可进行了审批");
    }
}
