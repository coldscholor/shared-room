package com.sharedroom.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sharedroom.notification.entity.Notification;
import com.sharedroom.notification.dto.NotificationDTO;
import com.sharedroom.notification.vo.NotificationVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 发送通知
     */
    NotificationVO sendNotification(NotificationDTO notificationDTO);

    /**
     * 批量发送通知
     */
    List<NotificationVO> batchSendNotification(List<NotificationDTO> notificationDTOs);

    /**
     * 根据ID获取通知详情
     */
    NotificationVO getNotificationById(Long id);

    /**
     * 获取用户通知列表（分页）
     */
    Page<NotificationVO> getUserNotifications(Page<Notification> page, Long userId, Integer type, Boolean isRead);

    /**
     * 获取当前用户通知列表（分页）
     */
    Page<NotificationVO> getCurrentUserNotifications(Page<Notification> page, Integer type, Boolean isRead);

    /**
     * 标记通知为已读
     */
    boolean markAsRead(Long id);

    /**
     * 批量标记通知为已读
     */
    boolean batchMarkAsRead(List<Long> ids);

    /**
     * 标记用户所有通知为已读
     */
    boolean markAllAsRead(Long userId);

    /**
     * 标记当前用户所有通知为已读
     */
    boolean markCurrentUserAllAsRead();

    /**
     * 删除通知
     */
    boolean deleteNotification(Long id);

    /**
     * 批量删除通知
     */
    boolean batchDeleteNotification(List<Long> ids);

    /**
     * 获取用户未读通知数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 获取当前用户未读通知数量
     */
    Long getCurrentUserUnreadCount();

    /**
     * 获取用户未读通知数量（按类型分组）
     */
    Map<String, Long> getUnreadCountByType(Long userId);

    /**
     * 获取当前用户未读通知数量（按类型分组）
     */
    Map<String, Long> getCurrentUserUnreadCountByType();

    /**
     * WebSocket推送通知
     */
    boolean pushNotificationByWebSocket(Long userId, NotificationVO notification);

    /**
     * 邮件推送通知
     */
    boolean pushNotificationByEmail(String email, String title, String content);

    /**
     * 短信推送通知
     */
    boolean pushNotificationBySms(String phone, String content);

    /**
     * 处理未推送的通知
     */
    void handleUnpushedNotifications();

    /**
     * 清理过期通知
     */
    void cleanExpiredNotifications();

    /**
     * 获取通知统计信息
     */
    Map<String, Object> getNotificationStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取在线用户数量
     */
    int getOnlineUserCount();

    /**
     * 检查用户是否在线
     */
    boolean isUserOnline(Long userId);

    /**
     * 广播系统通知
     */
    void broadcastSystemNotification(String title, String content);
}