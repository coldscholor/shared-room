package com.sharedroom.payment.interceptor;

import com.sharedroom.common.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户拦截器
 * 从请求头中提取用户信息并设置到上下文中
 */
@Component
public class UserInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(UserInterceptor.class);

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-Username";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取用户信息
        String userIdStr = request.getHeader(USER_ID_HEADER);
        String username = request.getHeader(USERNAME_HEADER);

        if (StringUtils.hasText(userIdStr)) {
            try {
                Long userId = Long.valueOf(userIdStr);
                UserContext.setUserId(userId);
                log.debug("设置用户ID: {}", userId);
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", userIdStr);
            }
        }

        if (StringUtils.hasText(username)) {
            UserContext.setUsername(username);
            log.debug("设置用户名: {}", username);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除用户上下文
        UserContext.clear();
    }
}