package com.sharedroom.common.utils;

/**
 * 用户上下文工具类
 * 基于ThreadLocal实现线程安全的用户信息传递
 */
public class UserContext {

    /**
     * 用户ID的ThreadLocal
     */
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 用户名的ThreadLocal
     */
    private static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 设置当前用户名
     *
     * @param username 用户名
     */
    public static void setUsername(String username) {
        USERNAME_HOLDER.set(username);
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        return USERNAME_HOLDER.get();
    }

    /**
     * 设置用户信息
     *
     * @param userId 用户ID
     * @param username 用户名
     */
    public static void setUser(Long userId, String username) {
        setUserId(userId);
        setUsername(username);
    }

    /**
     * 清除当前线程的用户信息
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        USERNAME_HOLDER.remove();
    }

    /**
     * 判断当前是否有用户登录
     *
     * @return 是否有用户登录
     */
    public static boolean hasUser() {
        return getUserId() != null;
    }
}