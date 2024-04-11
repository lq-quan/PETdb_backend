package com.szbldb.service.licenseService;

import com.szbldb.dao.LicenseMapper;
import com.szbldb.pojo.licensePojo.Submission;
import com.szbldb.pojo.licensePojo.SubmissionList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminLicenseService {
    @Autowired
    private LicenseMapper licenseMapper;

    public SubmissionList searchSubmissions(String word, Integer page, Integer limit){
        List<Submission> list = licenseMapper.searchSubmissions(word, (page - 1) * limit, limit);
        SubmissionList submissionList = new SubmissionList();
        submissionList.setItems(list);
        submissionList.setTotal(list.size());
        return submissionList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditSubmission(Integer id, String auditor, String status, String reason){
        licenseMapper.updateSubmissionById(id, auditor, status, reason);
    }
}
