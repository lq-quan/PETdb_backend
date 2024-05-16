package com.szbldb.service.licenseService;

import com.szbldb.dao.LicenseMapper;
import com.szbldb.pojo.licensePojo.Submission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;

@Service
public class ClientLicenseService {
    private final LicenseMapper licenseMapper;

    public ClientLicenseService(@Autowired LicenseMapper licenseMapper) {
        this.licenseMapper = licenseMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean createApplication(Submission submission, String username){
        if(licenseMapper.getSid(username) != null) return false;
        submission.setDate(LocalDate.now());
        licenseMapper.createApplication(submission);
        licenseMapper.appendUserAppl(username, submission.getId());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateApplication(Submission submission, String username){
        if(licenseMapper.getSid(username) == null) return false;
        submission.setDate(LocalDate.now());
        licenseMapper.updateApplicationByUsername(submission, username);
        return true;
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
        if(status == null){
            status = "none";
        }
        submission.setStatus(status);
        return submission;
    }

    public boolean checkIfVerified(Integer id){
        return licenseMapper.checkIfVerified(id) > 0;
    }

    public void insertValidEmail(Integer id, String email){
        Date date = new Date(System.currentTimeMillis() + 24 * 3600 * 1000L);
        licenseMapper.insertValidEmail(id, email, date);
    }
}
