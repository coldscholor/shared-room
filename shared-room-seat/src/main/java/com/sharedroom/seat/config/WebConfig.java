package com.sharedroom.seat.config;

import com.sharedroom.seat.interceptor.UserInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserInterceptor userInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/seat/page",
                        "/api/seat/*/",
                        "/api/seat/available/*",
                        "/api/seat/nearby",
                        "/api/seat/search",
                        "/api/seat/popular",
                        "/api/seat/available/check/*",
                        "/actuator/**",
                        "/error"
                );
    }
}