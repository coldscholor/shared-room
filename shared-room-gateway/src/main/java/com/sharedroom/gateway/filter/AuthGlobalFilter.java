package com.sharedroom.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.sharedroom.common.result.Result;
import com.sharedroom.common.result.ResultCode;
import com.sharedroom.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证全局过滤器
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 不需要认证的路径
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/user/login",
            "/user/register",
            "/user/captcha",
            "/payment/callback",
            "/actuator",
            "/doc.html",
            "/v2/api-docs",
            "/webjars",
            "/favicon.ico"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 检查是否为不需要认证的路径
        if (isExcludePath(path)) {
            return chain.filter(exchange);
        }
        
        // 获取Authorization头
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(exchange, "缺少认证信息");
        }
        
        // 提取JWT令牌
        String token = authHeader.substring(7);
        
        // 验证JWT令牌
        if (!JwtUtils.validateToken(token)) {
            return handleUnauthorized(exchange, "认证信息无效或已过期");
        }
        
        // 提取用户信息
        Long userId = JwtUtils.getUserId(token);
        String username = JwtUtils.getUsername(token);
        
        if (userId == null || !StringUtils.hasText(username)) {
            return handleUnauthorized(exchange, "用户信息无效");
        }
        
        // 将用户信息添加到请求头中，传递给下游服务
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username)
                .build();
        
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        
        log.debug("用户认证成功: userId={}, username={}, path={}", userId, username, path);
        
        return chain.filter(modifiedExchange);
    }
    
    /**
     * 检查是否为不需要认证的路径
     */
    private boolean isExcludePath(String path) {
        return EXCLUDE_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 处理未授权请求
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), message);
        String body = JSON.toJSONString(result);
        
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -100;
    }
}