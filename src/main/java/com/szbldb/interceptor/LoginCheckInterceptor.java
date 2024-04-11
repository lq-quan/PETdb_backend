package com.szbldb.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.szbldb.dao.UserMapper;
import com.szbldb.pojo.Result;
import com.szbldb.util.JWTHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Autowired
    private UserMapper userMapper;
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String jwtUser = request.getHeader("token");
        System.out.println("interceptor: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + jwtUser);
        if(!StringUtils.hasLength(jwtUser)){
            Result error = Result.error("Not_Login", 50008);
            String notLogin = JSONObject.toJSONString(error);
            response.getWriter().write(notLogin);
            return false;
        }
        try{
            if(userMapper.checkLogout(jwtUser) != 0) throw new RuntimeException("The token was destroyed by logging out");
            JWTHelper.jwtUnpack(jwtUser);
        }catch (Exception e){
            Result error = Result.error("Expired", 50007);
            String expired = JSONObject.toJSONString(error);
            response.getWriter().write(expired);
            return false;
        }
        return true;
    }
}
