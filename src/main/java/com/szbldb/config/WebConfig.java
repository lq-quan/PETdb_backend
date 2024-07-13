package com.szbldb.config;

import com.szbldb.interceptor.AdminCheckInterceptor;
import com.szbldb.interceptor.EmailCheckInterceptor;
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
    private final EmailCheckInterceptor emailCheckInterceptor;//邮箱验证拦截器

    /**
     *
     *
     * @param loginCheckInterceptor 登录检查拦截器
     * @param adminCheckInterceptor 管理员检查拦截器
     * @param licenseCheckInterceptor 许可证检查拦截器
     * @param emailCheckInterceptor 邮箱验证拦截器
     * @author Quan Li 2024/7/11 20:41
     **/
    @Autowired
    public WebConfig(LoginCheckInterceptor loginCheckInterceptor, AdminCheckInterceptor adminCheckInterceptor,
                     LicenseCheckInterceptor licenseCheckInterceptor, EmailCheckInterceptor emailCheckInterceptor){
        this.loginCheckInterceptor = loginCheckInterceptor;
        this.adminCheckInterceptor = adminCheckInterceptor;
        this.licenseCheckInterceptor = licenseCheckInterceptor;
        this.emailCheckInterceptor = emailCheckInterceptor;
    }

    /**
     *
     * @param registry 拦截器注册表
     * @author Quan Li 2024/7/3 18:08
     **/
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(loginCheckInterceptor).addPathPatterns("/**")
                .excludePathPatterns(List.of("/PETdatabase/user/login/**", "/PETdatabase/register/**",
                        "/PETdatabase/dataset/list", "/PETdatabase/dataset/license/status",
                        "/PETdatabase/extended/collection/list", "/PETdatabase/extended/news/list"));
        registry.addInterceptor(adminCheckInterceptor).addPathPatterns("/PETdatabase/dataset/license/admin/**")
                .addPathPatterns("/PETdatabase/extended/admin/**")
                .addPathPatterns("/PETdatabase/dataset/manage/**")
                .addPathPatterns("/PETdatabase/logs")
                .addPathPatterns("/PETdatabase/extended/news/admin/**");
        registry.addInterceptor(licenseCheckInterceptor).addPathPatterns("/PETdatabase/dataset/download*");
        registry.addInterceptor(emailCheckInterceptor).addPathPatterns("/PETdatabase/dataset/license/verify*");
    }
}
