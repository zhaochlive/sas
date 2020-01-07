package com.js.sas.config;

import com.js.sas.Interceptor.LoginHandlerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfg implements WebMvcConfigurer {

    public final static String SYSTEM_USER = "SYSTEM_USER";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginHandlerInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/"
                        , "/login"
//                        ,"/**"
                        , "/api/**"
                        , "/login_out"
                        , "/login_err"
                        , "/static/**"
                        , "/css/**"
                        , "/plugins/**"
                        , "/js/**"
                        , "/icon/**"
                        , "/image/**"
                );
    }

}