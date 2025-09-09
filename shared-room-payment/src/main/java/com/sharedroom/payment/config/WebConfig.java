package com.sharedroom.payment.config;

import com.sharedroom.payment.interceptor.UserInterceptor;
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
                        "/api/payments/statistics",
                        "/api/payments/handle-expired",
                        "/api/payments/alipay/notify",
                        "/actuator/**",
                        "/error"
                );
    }
}