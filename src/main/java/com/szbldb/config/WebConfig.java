package com.szbldb.config;

import com.szbldb.interceptor.AdminCheckInterceptor;
import com.szbldb.interceptor.LicenseCheckInterceptor;
import com.szbldb.interceptor.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    private final LoginCheckInterceptor loginCheckInterceptor;//登录检查拦截器

    private final AdminCheckInterceptor adminCheckInterceptor;//管理员检查拦截器

    private final LicenseCheckInterceptor licenseCheckInterceptor;//许可证检查拦截器

    /**
     *
     * @param loginCheckInterceptor 登录检查拦截器
     * @param adminCheckInterceptor 管理员检查拦截器
     * @author Quan Li 2024/7/3 18:07
     **/
    @Autowired
    public WebConfig(LoginCheckInterceptor loginCheckInterceptor, AdminCheckInterceptor adminCheckInterceptor,
                     LicenseCheckInterceptor licenseCheckInterceptor){
        this.loginCheckInterceptor = loginCheckInterceptor;
        this.adminCheckInterceptor = adminCheckInterceptor;
        this.licenseCheckInterceptor = licenseCheckInterceptor;
    }

    /**
     *
     * @param registry 拦截器注册表
     * @author Quan Li 2024/7/3 18:08
     **/
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(loginCheckInterceptor).addPathPatterns("/**")
                .excludePathPatterns(List.of("/PETdatabase/user/login/**", "/PETdatabase/register/**", "/PETdatabase/dataset/list"));
        registry.addInterceptor(adminCheckInterceptor).addPathPatterns("/PETdatabase/dataset/license/admin/**")
                .addPathPatterns("/PETdatabase/extended/admin/**")
                .addPathPatterns("/PETdatabase/dataset/manage/**")
                .addPathPatterns("/PETdatabase/logs");
        registry.addInterceptor(licenseCheckInterceptor).addPathPatterns("/PETdatabase/dataset/download*");
    }
}
