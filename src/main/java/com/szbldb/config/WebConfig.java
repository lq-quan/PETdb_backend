package com.szbldb.config;

import com.szbldb.interceptor.AdminCheckInterceptor;
import com.szbldb.interceptor.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    private final LoginCheckInterceptor loginCheckInterceptor;

    private final AdminCheckInterceptor adminCheckInterceptor;

    @Autowired
    public WebConfig(LoginCheckInterceptor loginCheckInterceptor, AdminCheckInterceptor adminCheckInterceptor){
        this.loginCheckInterceptor = loginCheckInterceptor;
        this.adminCheckInterceptor = adminCheckInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(loginCheckInterceptor).addPathPatterns("/**")
                .excludePathPatterns(List.of("/PETdatabase/user/login/**", "/PETdatabase/register/**"));
        registry.addInterceptor(adminCheckInterceptor).addPathPatterns("/PETdatabase/dataset/license/admin/**")
                .addPathPatterns("/PETdatabase/extended/admin/**");
    }
}
