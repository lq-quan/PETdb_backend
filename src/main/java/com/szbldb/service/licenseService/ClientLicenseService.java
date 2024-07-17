package com.szbldb.service.licenseService;

import com.szbldb.dao.LicenseMapper;
import com.szbldb.pojo.licensePojo.Submission;
import com.szbldb.util.MailHelper;
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

    /**
     *
     * @Description 用户创建/提交申请
     * @param submission 申请具体信息
     * @param username 用户名
     * @return boolean
     * @author Quan Li 2024/7/5 16:06
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean createApplication(Submission submission, String username){
        if(licenseMapper.getSid(username) != null) return false;
        submission.setDate(LocalDate.now());
        licenseMapper.createApplication(submission);
        licenseMapper.appendUserAppl(username, submission.getId());
        MailHelper.informReceiveApplication();
        return true;
    }

    /**
     *
     * @Description 通过用户名获取邮箱
     * @param username 用户名
     * @return java.lang.String
     * @author Quan Li 2024/7/11 20:32
     **/
    public String getEmail(String username){
        return licenseMapper.getEmailByUsername(username);
    }

    /**
     *
     * @Description 用户修改申请
     * @param submission 修改后的信息
     * @param username 用户名
     * @return boolean
     * @author Quan Li 2024/7/5 16:06
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean updateApplication(Submission submission, String username){
        Integer sid = licenseMapper.getSid(username);
        if(sid == null) return false;
        submission.setDate(LocalDate.now());
        licenseMapper.updateApplicationByUsername(submission, username);
        licenseMapper.updateSubmissionById(sid, null, "pending", null);
        MailHelper.informReceiveApplication();
        return true;
    }

    /**
     *
     * @Description 通过申请 id 检查申请的具体信息
     * @param sid 申请 id
     * @return com.szbldb.pojo.licensePojo.Submission
     * @author Quan Li 2024/7/5 16:07
     **/
    public Submission checkApplicationBySid(Integer sid){
        return licenseMapper.checkApplication(sid);
    }

    /**
     *
     * @Description 通过用户名获取申请信息
     * @param username 用户名
     * @return com.szbldb.pojo.licensePojo.Submission
     * @author Quan Li 2024/7/5 16:07
     **/
    @Transactional(rollbackFor = Exception.class)
    public Submission checkApplicationByUsername(String username){
        Integer sid = licenseMapper.getSid(username);
        return checkApplicationBySid(sid);
    }

    /**
     *
     * @Description 通过用户名检查申请状态
     * @param username 用户名
     * @return com.szbldb.pojo.licensePojo.Submission
     * @author Quan Li 2024/7/5 16:08
     **/
    public Submission getStatusByUsername(String username){
        String status = licenseMapper.getStatusByUsername(username);
        Submission submission = new Submission();
        if(status == null){
            status = "none";
        }
        submission.setStatus(status);
        return submission;
    }

    /**
     *
     * @Description 检查用户是否已通过邮箱验证
     * @param username 用户名
     * @return boolean
     * @author Quan Li 2024/7/5 16:09
     **/
    public boolean checkIfVerified(String username){
        return licenseMapper.checkIfVerifiedByUsername(username) > 0;
    }


    /**
     *
     * @Description 用户通过邮箱验证后，修改数据库
     * @param username 用户名
     * @author Quan Li 2024/7/5 16:11
     **/
    public void insertValidEmail(String username){
        Date date = new Date(System.currentTimeMillis() + 24 * 3600 * 1000L);
        licenseMapper.insertValidEmail(username, date);
    }
}
