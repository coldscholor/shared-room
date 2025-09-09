package com.sharedroom.notification.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器
 */
@Component
@RequiredArgsConstructor
public class WebSocketHandler implements org.springframework.web.socket.WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);
    private final ObjectMapper objectMapper;

    // 存储用户ID与WebSocket会话的映射关系
    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();
    // 存储会话ID与用户ID的映射关系
    private static final Map<String, Long> SESSION_USERS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            // 从URL参数中获取用户ID
            Long userId = getUserIdFromSession(session);
            if (userId != null) {
                USER_SESSIONS.put(userId, session);
                SESSION_USERS.put(session.getId(), userId);
                log.info("用户 {} 建立WebSocket连接，会话ID: {}", userId, session.getId());
                
                // 发送连接成功消息
                sendMessage(session, createMessage("connection", "连接成功", null));
            } else {
                log.warn("WebSocket连接缺少用户ID参数，会话ID: {}", session.getId());
                session.close(CloseStatus.BAD_DATA.withReason("缺少用户ID参数"));
            }
        } catch (Exception e) {
            log.error("建立WebSocket连接异常", e);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = message.getPayload().toString();
            log.debug("收到WebSocket消息: {}", payload);
            
            // 解析消息
            Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);
            String type = (String) messageMap.get("type");
            
            switch (type) {
                case "ping":
                    // 心跳检测
                    sendMessage(session, createMessage("pong", "心跳响应", null));
                    break;
                case "markRead":
                    // 标记消息已读
                    handleMarkRead(session, messageMap);
                    break;
                default:
                    log.warn("未知的消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息异常", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输异常，会话ID: {}", session.getId(), exception);
        removeSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Long userId = SESSION_USERS.get(session.getId());
        log.info("用户 {} WebSocket连接关闭，会话ID: {}，关闭状态: {}", userId, session.getId(), closeStatus);
        removeSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 向指定用户推送消息
     */
    public boolean pushToUser(Long userId, Map<String, Object> message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, message);
                return true;
            } catch (Exception e) {
                log.error("向用户 {} 推送消息失败", userId, e);
                removeSession(session);
            }
        }
        return false;
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(Map<String, Object> message) {
        USER_SESSIONS.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    sendMessage(session, message);
                } catch (Exception e) {
                    log.error("向用户 {} 广播消息失败", userId, e);
                    removeSession(session);
                }
            } else {
                removeSession(session);
            }
        });
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return USER_SESSIONS.size();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 从会话中获取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri != null) {
                String query = uri.getQuery();
                if (query != null) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2 && "userId".equals(keyValue[0])) {
                            return Long.valueOf(keyValue[1]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析用户ID异常", e);
        }
        return null;
    }

    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, Map<String, Object> message) throws IOException {
        String json = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(json));
    }

    /**
     * 创建消息
     */
    private Map<String, Object> createMessage(String type, String content, Object data) {
        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("type", type);
        message.put("content", content);
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    /**
     * 处理标记已读消息
     */
    private void handleMarkRead(WebSocketSession session, Map<String, Object> messageMap) {
        // 这里可以调用通知服务标记消息为已读
        log.info("处理标记已读消息: {}", messageMap);
    }

    /**
     * 移除会话
     */
    private void removeSession(WebSocketSession session) {
        Long userId = SESSION_USERS.remove(session.getId());
        if (userId != null) {
            USER_SESSIONS.remove(userId);
        }
    }
}