package com.szbldb.service.userService;

import com.szbldb.dao.UserMapper;
import com.szbldb.pojo.userPojo.User;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
public class RegisterService {

    private final UserMapper userMapper;

    @Autowired
    public RegisterService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * @Description 检查用户名是否重复
     * @param username 用户名
     * @return boolean
     * @author Quan Li 2024/7/5 16:25
     **/
    public boolean checkIfExisted(String username){
        if(username.length() > 50 || "default".equals(username)) return false;
        return userMapper.getUserByUsername(username) == null;
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
     * @Description 通过用户名获取用户 id
     * @param username 用户名
     * @return com.szbldb.pojo.userPojo.User
     * @author Quan Li 2024/7/5 16:26
     **/
    public User getIdByUsername(String username){
        return userMapper.getUserByUsername(username);
    }

}