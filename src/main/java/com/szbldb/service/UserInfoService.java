package com.szbldb.service;

import com.szbldb.dao.UserMapper;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import com.szbldb.util.JWTHelper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserInfoService {
    @Autowired
    UserMapper userMapper;

    public UserInfo getUserInfo(String jwtUser){
        Claims claims = JWTHelper.jwtUnpack(jwtUser);
        String username = claims.get("username", String.class);
        Integer id = userMapper.getIdByName(username);
        return userMapper.getInfoById(id);
    }

    public void changeInfo(String token, UserInfo userInfo){
        Claims claims = JWTHelper.jwtUnpack(token);
        String username = claims.get("username", String.class);
        Integer id = userMapper.getIdByName(username);
        userInfo.setId(id);
        userMapper.changeUserInfo(userInfo);
    }
}
