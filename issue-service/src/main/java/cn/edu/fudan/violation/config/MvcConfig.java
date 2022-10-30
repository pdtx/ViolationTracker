package cn.edu.fudan.violation.config;


import cn.edu.fudan.violation.interceptor.AuthTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Bean
    public AuthTokenInterceptor authTokenInterceptor() {
        return new AuthTokenInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/issue/**");
        urlPatterns.add("/raw-issue/**");
        urlPatterns.add("/measurement/**");
        registry.addInterceptor(authTokenInterceptor()).addPathPatterns(urlPatterns);
    }

}
