package com.szbldb.service.userService;

import com.szbldb.dao.UserMapper;
import com.szbldb.exception.UserException;
import com.szbldb.pojo.userPojo.User;
import com.szbldb.util.JWTHelper;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LoginService {

    private final UserMapper userMapper;

    public LoginService(@Autowired UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public boolean check(String username, String password) throws UserException{
        User user = userMapper.getUserByUsername(username);
        if(user == null) return false;
        boolean correctOrNot = user.getPassword().equals(password);
        if(correctOrNot && "admin".equals(userMapper.getRolesByUsername(username)))
            throw new UserException("检测到管理员登录：" + username);
        return correctOrNot;
    }

    public void logout(String token){
        Claims claims = JWTHelper.jwtUnpack(token);
        String username = claims.get("username", String.class);
        Date date = new Date(System.currentTimeMillis() + 24 * 3600 * 1000L);
        userMapper.logout(username, token, date);
    }

    public String getEmail(String username){
        return userMapper.getEmail(username);
    }
}
