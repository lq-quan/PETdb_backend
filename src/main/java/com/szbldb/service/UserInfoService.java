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
        Claims claims;
        try{
            claims = JWTHelper.jwtUnpack(jwtUser);
        }catch (ExpiredJwtException e){
            return null;
        }
        String username = claims.get("username", String.class);
        Integer id = userMapper.getIdByName(username);
        return userMapper.getInfoById(id);
    }
}
