package com.szbldb.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.szbldb.dao.LicenseMapper;
import com.szbldb.pojo.Result;
import com.szbldb.util.JWTHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class EmailCheckInterceptor implements HandlerInterceptor {
    private final LicenseMapper licenseMapper;

    @Autowired
    public EmailCheckInterceptor(LicenseMapper licenseMapper){
        this.licenseMapper = licenseMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception{
        String token = request.getHeader("token");
        String username = JWTHelper.getUsername(token);
        if(licenseMapper.checkIfVerifiedByUsername(username) > 0){
            Result verified = Result.error("You don't need to verify email!", 50102);
            String json = JSONObject.toJSONString(verified);
            response.getWriter().write(json);
            return false;
        }
        return true;
    }
}
