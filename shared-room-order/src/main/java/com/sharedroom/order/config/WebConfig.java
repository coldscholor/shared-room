package com.sharedroom.order.config;

import com.sharedroom.order.interceptor.UserInterceptor;
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
                        "/api/orders/statistics",     // 统计接口
                        "/api/orders/handle-expired", // 定时任务接口
                        "/actuator/**",               // 监控端点
                        "/error"                      // 错误页面
                );
    }
}