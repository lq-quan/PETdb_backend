package com.szbldb.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.szbldb.pojo.Result;
import com.szbldb.util.JWTHelper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String jwtUser = request.getHeader("token");
        if(!StringUtils.hasLength(jwtUser)){
            Result error = Result.error("Not_Login", 50008);
            String notLogin = JSONObject.toJSONString(error);
            response.getWriter().write(notLogin);
            return false;
        }
        try{
            JWTHelper.jwtUnpack(jwtUser);
        }catch (ExpiredJwtException e){
            Result error = Result.error("Expired", 50007);
            String expired = JSONObject.toJSONString(error);
            response.getWriter().write(expired);
            return false;
        }
        return true;
    }
}
