package com.sharedroom.notification.config;

import com.sharedroom.notification.interceptor.UserInterceptor;
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
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/notifications/statistics",
                        "/api/notifications/handle-unpushed",
                        "/api/notifications/clean-expired",
                        "/api/notifications/online-count",
                        "/api/notifications/broadcast",
                        "/ws/**",
                        "/actuator/**",
                        "/error"
                );
    }
}