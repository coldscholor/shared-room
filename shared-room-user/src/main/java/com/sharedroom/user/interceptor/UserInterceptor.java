package com.sharedroom.user.interceptor;

import com.sharedroom.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户信息拦截器
 * 从请求头中提取用户信息并存储到ThreadLocal
 */
@Slf4j
@Component
public class UserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取用户信息
        String userIdHeader = request.getHeader("X-User-Id");
        String usernameHeader = request.getHeader("X-Username");
        
        if (StringUtils.hasText(userIdHeader) && StringUtils.hasText(usernameHeader)) {
            try {
                Long userId = Long.valueOf(userIdHeader);
                UserContext.setUser(userId, usernameHeader);
                log.debug("用户信息已设置到上下文: userId={}, username={}", userId, usernameHeader);
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", userIdHeader);
            }
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除ThreadLocal中的用户信息，防止内存泄漏
        UserContext.clear();
    }
}