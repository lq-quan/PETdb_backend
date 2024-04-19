package com.szbldb.service.licenseService;

import com.szbldb.dao.LicenseMapper;
import com.szbldb.pojo.licensePojo.Submission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ClientLicenseService {
    private final LicenseMapper licenseMapper;

    public ClientLicenseService(@Autowired LicenseMapper licenseMapper) {
        this.licenseMapper = licenseMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createApplication(Submission submission, String username){
        submission.setDate(LocalDate.now());
        licenseMapper.createApplication(submission);
        licenseMapper.appendUserAppl(username, submission.getId());
    }

    public Submission checkApplicationBySid(Integer sid){
        return licenseMapper.checkApplication(sid);
    }

    @Transactional(rollbackFor = Exception.class)
    public Submission checkApplicationByUsername(String username){
        Integer sid = licenseMapper.getSid(username);
        return checkApplicationBySid(sid);
    }

    public Submission getStatusByUsername(String username){
        String status = licenseMapper.getStatusByUsername(username);
        Submission submission = new Submission();
        submission.setStatus(status);
        return submission;
    }
}
