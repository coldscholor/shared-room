package com.sharedroom.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Slf4j
public class JwtUtils {

    /**
     * JWT密钥
     */
    private static final String SECRET = "shared-room-jwt-secret-key-2025";

    /**
     * JWT过期时间(毫秒) - 7天
     */
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成JWT令牌
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT令牌
     */
    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return createToken(claims);
    }

    /**
     * 创建JWT令牌
     *
     * @param claims 载荷信息
     * @return JWT令牌
     */
    private static String createToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + EXPIRE_TIME);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    /**
     * 解析JWT令牌
     *
     * @param token JWT令牌
     * @return 载荷信息
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("JWT令牌解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证JWT令牌是否有效
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断JWT令牌是否过期
     *
     * @param claims 载荷信息
     * @return 是否过期
     */
    private static boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 从JWT令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }

    /**
     * 从JWT令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }

    /**
     * 获取JWT令牌的过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public static Date getExpirationDate(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getExpiration() : null;
    }
}