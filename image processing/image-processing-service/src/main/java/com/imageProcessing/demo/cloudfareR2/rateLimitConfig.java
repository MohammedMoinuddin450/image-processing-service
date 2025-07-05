package com.imageProcessing.demo.cloudfareR2;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class rateLimitConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitingFilter() {
        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitFilter());
        registrationBean.addUrlPatterns("/images/*");
        return registrationBean;
    }
}

