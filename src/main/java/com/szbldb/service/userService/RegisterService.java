package com.szbldb.service.userService;

import com.szbldb.dao.UserMapper;
import com.szbldb.pojo.userPojo.User;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import com.szbldb.service.logService.LogService;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
public class RegisterService {

    private final UserMapper userMapper;
    private final LogService logService;

    @Autowired
    public RegisterService(UserMapper userMapper, LogService logService) {
        this.userMapper = userMapper;
        this.logService = logService;
    }

    /**
     * @Description 检查用户名是否重复
     * @param username 用户名
     * @return boolean
     * @author Quan Li 2024/7/5 16:25
     **/
    public boolean checkIfExisted(String username){
        if(username.length() > 50 || "default".equals(username)) return false;
        return userMapper.getIdByName(username) == 0;
    }

    /**
     *
     * @Description 用户注册并初始化用户信息
     * @param username 用户名
     * @param password 经 Bcrypt 加密的密码
     * @param email 邮箱
     * @return boolean
     * @author Quan Li 2024/7/5 16:25
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean signup(String username, String password, String email){
        User user = new User(username, password, email);
        userMapper.insertUser(user);
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setName(username);
        userMapper.initUserInfo(userInfo);
        return true;
    }

    /**
     *
     * @Description 查询管理员列表
     * @param page （第）页数
     * @param limit 每页项数
     * @return java.util.List<com.szbldb.pojo.userPojo.User>
     * @author Quan Li 2024/7/17 10:33
     **/
    public List<User> getAdmins(Integer page, Integer limit){
        return userMapper.getAdmins(limit, (page - 1) * limit);
    }

    /**
     *
     * @Description 创建管理员账号
     * @param user 账号信息
     * @return boolean
     * @author Quan Li 2024/7/17 10:33
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean createAdmin(User user){
        String username = user.getUsername();
        if(!checkIfExisted(username)) return false;
        String encodedPsw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        signup(username, encodedPsw, user.getEmail());
        userMapper.insertAdmin(username);
        logService.addLog("创建管理员账号：" + username);
        return true;
    }

    /**
     *
     * @Description 删除指定管理员账号
     * @param id 待删除的账号 id
     * @author Quan Li 2024/7/17 10:34
     **/
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdmin(Integer id){
        userMapper.deleteAdminById(id);
        userMapper.deleteUserinfo(id);
        userMapper.deleteUser(id);
        logService.addLog("回收管理员账号：" + userMapper.getUsernameById(id));
    }
}