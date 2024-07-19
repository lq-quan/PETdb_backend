package com.szbldb.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.szbldb.dao.UserMapper;
import com.szbldb.pojo.Result;
import com.szbldb.service.logService.LogService;
import com.szbldb.util.JWTHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;
    private final LogService logService;

    public LoginCheckInterceptor(@Autowired UserMapper userMapper, @Autowired LogService logService) {
        this.logService = logService;
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String jwtUser = request.getHeader("token");
        String username;
        System.out.println("interceptor: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + jwtUser);
        if(!StringUtils.hasLength(jwtUser)){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Result error = Result.error("Not_Login", 50008);
            String notLogin = JSONObject.toJSONString(error);
            response.getWriter().write(notLogin);
            return false;
        }
        try{
            if(userMapper.checkLogout(jwtUser) != 0) throw new RuntimeException("The token was destroyed by logging out");
            username = JWTHelper.getUsername(jwtUser);
        }catch (Exception e){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Result error = Result.error("Expired", 50007);
            String expired = JSONObject.toJSONString(error);
            response.getWriter().write(expired);
            return false;
        }
        if("admin".equals(userMapper.getRolesByUsername(username))){
            String ipAddress = request.getRemoteAddr();
            String curAddr = userMapper.checkIpAddrOfAdmin(username);
            if(ipAddress.equals(curAddr) && jwtUser.equals(userMapper.checkTokenOfAdmin(username))){
                logService.setUser(username);
                return true;
            }
            log.warn("Admin 异地登录或令牌失效：" + ipAddress);
            Result logout = Result.error("You have been logged out.", 50007);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(JSONObject.toJSONString(logout));
            return false;
        }
        logService.setUser(username);
        return true;
    }
}
