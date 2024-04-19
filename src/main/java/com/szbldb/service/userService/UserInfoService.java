package com.szbldb.service.userService;

import com.szbldb.dao.UserMapper;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import com.szbldb.util.JWTHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserInfoService {

    private final UserMapper userMapper;

    public UserInfoService(@Autowired UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserInfo getUserInfo(String jwtUser){
        String username = JWTHelper.getUsername(jwtUser);
        Integer id = userMapper.getIdByName(username);
        return userMapper.getInfoById(id);
    }

    public void changeInfo(String token, UserInfo userInfo){
        String username = JWTHelper.getUsername(token);
        Integer id = userMapper.getIdByName(username);
        userInfo.setId(id);
        userMapper.changeUserInfo(userInfo);
    }

    public String getEmail(String token){
        String username = JWTHelper.getUsername(token);
        return userMapper.getEmail(username);
    }
}
