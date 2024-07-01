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

    public boolean checkIfExisted(String username){
        return userMapper.getUserByUsername(username) == null;
    }

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

    public User getIdByUsername(String username){
        return userMapper.getUserByUsername(username);
    }

}