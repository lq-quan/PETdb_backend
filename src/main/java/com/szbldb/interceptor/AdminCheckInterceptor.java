package com.szbldb.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.szbldb.dao.UserMapper;
import com.szbldb.pojo.Result;
import com.szbldb.util.JWTHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class AdminCheckInterceptor implements HandlerInterceptor {
    private final UserMapper userMapper;

    @Autowired
    public AdminCheckInterceptor(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String token = request.getHeader("token");
        if(token == null || token.isEmpty()) return false;
        String username = JWTHelper.getUsername(token);
        if(!"admin".equals(userMapper.getRolesByUsername(username))){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Result error = Result.error("Not_Admin", 52002);
            String notAdmin = JSONObject.toJSONString(error);
            response.getWriter().write(notAdmin);
            return false;
        }
        return true;
    }
}
