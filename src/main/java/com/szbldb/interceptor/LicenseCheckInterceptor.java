package com.szbldb.interceptor;


import com.szbldb.dao.LicenseMapper;
import com.szbldb.dao.UserMapper;
import com.szbldb.util.JWTHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
public class LicenseCheckInterceptor implements HandlerInterceptor {

    private final LicenseMapper licenseMapper;
    private final UserMapper userMapper;

    @Autowired
    public LicenseCheckInterceptor(LicenseMapper licenseMapper, UserMapper userMapper) {
        this.licenseMapper = licenseMapper;
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws IOException {
        String token = request.getHeader("token");
        if(token == null || token.isEmpty()) return false;
        String username = JWTHelper.getUsername(token);
        if("admin".equals(userMapper.getRolesByUsername(username))) return true;
        else{
            if("approved".equals(licenseMapper.getStatusByUsername(username))) return true;
        }
        log.warn("用户未通过申请，但试图下载数据：" + username);
        response.sendError(403);
        return false;
    }
}
