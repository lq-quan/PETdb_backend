package com.szbldb.service.userService;

import com.szbldb.dao.UserMapper;
import com.szbldb.exception.UserException;
import com.szbldb.pojo.userPojo.User;
import com.szbldb.service.logService.LogService;
import com.szbldb.util.JWTHelper;
import io.jsonwebtoken.Claims;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.util.Date;


@Service
public class LoginService {

    private final UserMapper userMapper;
    private final LogService logService;

    @Autowired
    public LoginService(UserMapper userMapper, LogService logService) {
        this.userMapper = userMapper;
        this.logService = logService;
    }

    /**
     *
     * @Description 登录检查
     * @param username 用户名
     * @param password 密码
     * @return boolean
     * @author Quan Li 2024/7/5 16:23
     **/
    public boolean check(String username, String password) throws UserException{
        User user = userMapper.getUserByUsername(username);
        if(user == null) return false;
        String hashPsw = user.getPassword();
        boolean correctOrNot = BCrypt.checkpw(password, hashPsw);
        if(correctOrNot && "admin".equals(userMapper.getRolesByUsername(username)))
            throw new UserException("检测到管理员登录：" + username);
        return correctOrNot;
    }

    /**
     *
     * @Description 更新管理员登录信息
     * @param username 用户名
     * @param ipAddress IP 地址
     * @param token 令牌
     * @author Quan Li 2024/7/5 16:23
     **/
    //@Transactional(rollbackFor = Exception.class)
    public void updateAdmin(String username, String ipAddress, String token){
        logService.setUser(username);
        logService.addLog("成功：管理员 " + username + " 登录，IP：" + ipAddress);
        Date expireTime = new Date(System.currentTimeMillis() + 24 * 3600 * 1000L);
        userMapper.updateAdmin(username, ipAddress, token, expireTime);
    }

    /**
     *
     * @Description 用户登出，注销令牌
     * @param token 令牌
     * @author Quan Li 2024/7/5 16:24
     **/
    public void logout(String token){
        Claims claims = JWTHelper.jwtUnpack(token);
        String username = claims.get("username", String.class);
        Date date = new Date(System.currentTimeMillis() + 24 * 3600 * 1000L);
        if(userMapper.checkLogout(token) == 1) return;
        userMapper.logout(username, token, date);
    }

    /**
     *
     * @Description 获取用户邮箱
     * @param username 用户名
     * @return java.lang.String
     * @author Quan Li 2024/7/11 20:51
     **/
    public String getEmail(String username) {
        return userMapper.getEmail(username);
    }

}
