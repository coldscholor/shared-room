package com.sharedroom.order.interceptor;

import com.sharedroom.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户信息拦截器
 * 从请求头中提取用户信息并存储到ThreadLocal中
 */
@Slf4j
@Component
public class UserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取用户信息（由Gateway传递）
        String userIdHeader = request.getHeader("X-User-Id");
        String usernameHeader = request.getHeader("X-Username");
        
        if (userIdHeader != null) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                UserContext.setUserId(userId);
                UserContext.setUsername(usernameHeader);
                log.debug("设置用户上下文: userId={}, username={}", userId, usernameHeader);
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", userIdHeader);
            }
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}