package com.atguigu.gamll.config;

import com.atguigu.gamll.interceptors.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 将拦截器注入到spring容器中
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    AuthInterceptor authInterceptor;

    /**
     * addInterceptors:将拦截器加入到spring容器中
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(authInterceptor).addPathPatterns("/**").excludePathPatterns("/error");
//        registry.addInterceptor(authInterceptor).excludePathPatterns("*error*"); //排除的请求
        super.addInterceptors(registry);
    }
}










