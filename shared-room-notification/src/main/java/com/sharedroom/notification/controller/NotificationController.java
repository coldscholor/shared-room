package com.sharedroom.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sharedroom.notification.entity.Notification;
import com.sharedroom.common.result.Result;
import com.sharedroom.notification.dto.NotificationDTO;
import com.sharedroom.notification.service.NotificationService;
import com.sharedroom.notification.vo.NotificationVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 发送通知
     */
    @PostMapping
    public Result<NotificationVO> sendNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        NotificationVO notificationVO = notificationService.sendNotification(notificationDTO);
        return Result.success(notificationVO);
    }

    /**
     * 批量发送通知
     */
    @PostMapping("/batch")
    public Result<List<NotificationVO>> batchSendNotification(@Valid @RequestBody List<NotificationDTO> notificationDTOs) {
        List<NotificationVO> notifications = notificationService.batchSendNotification(notificationDTOs);
        return Result.success(notifications);
    }

    /**
     * 根据ID获取通知详情
     */
    @GetMapping("/{id}")
    public Result<NotificationVO> getNotificationById(
            @PathVariable @NotNull Long id) {
        NotificationVO notificationVO = notificationService.getNotificationById(id);
        return Result.success(notificationVO);
    }

    /**
     * 获取当前用户通知列表（分页）
     */
    @GetMapping("/my")
    public Result<Page<NotificationVO>> getCurrentUserNotifications(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Boolean isRead) {
        Page<Notification> page = new Page<>(current, size);
        Page<NotificationVO> notificationPage = notificationService.getCurrentUserNotifications(page, type, isRead);
        return Result.success(notificationPage);
    }

    /**
     * 获取指定用户通知列表（分页）
     */
    @GetMapping("/user/{userId}")
    public Result<Page<NotificationVO>> getUserNotifications(
            @PathVariable @NotNull Long userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Boolean isRead) {
        Page<Notification> page = new Page<>(current, size);
        Page<NotificationVO> notificationPage = notificationService.getUserNotifications(page, userId, type, isRead);
        return Result.success(notificationPage);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(
            @PathVariable @NotNull Long id) {
        boolean success = notificationService.markAsRead(id);
        return Result.success(success);
    }

    /**
     * 批量标记通知为已读
     */
    @PutMapping("/batch-read")
    public Result<Boolean> batchMarkAsRead(
            @RequestBody @NotEmpty List<Long> ids) {
        boolean success = notificationService.batchMarkAsRead(ids);
        return Result.success(success);
    }

    /**
     * 标记当前用户所有通知为已读
     */
    @PutMapping("/read-all")
    public Result<Boolean> markCurrentUserAllAsRead() {
        boolean success = notificationService.markCurrentUserAllAsRead();
        return Result.success(success);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteNotification(
            @PathVariable @NotNull Long id) {
        boolean success = notificationService.deleteNotification(id);
        return Result.success(success);
    }

    /**
     * 批量删除通知
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteNotification(
            @RequestBody @NotEmpty List<Long> ids) {
        boolean success = notificationService.batchDeleteNotification(ids);
        return Result.success(success);
    }

    /**
     * 获取当前用户未读通知数量
     */
    @GetMapping("/unread-count")
    public Result<Long> getCurrentUserUnreadCount() {
        Long count = notificationService.getCurrentUserUnreadCount();
        return Result.success(count);
    }

    /**
     * 获取当前用户未读通知数量（按类型分组）
     */
    @GetMapping("/unread-count-by-type")
    public Result<Map<String, Long>> getCurrentUserUnreadCountByType() {
        Map<String, Long> countMap = notificationService.getCurrentUserUnreadCountByType();
        return Result.success(countMap);
    }

    /**
     * 获取指定用户未读通知数量
     */
    @GetMapping("/user/{userId}/unread-count")
    public Result<Long> getUnreadCount(
            @PathVariable @NotNull Long userId) {
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 获取指定用户未读通知数量（按类型分组）
     */
    @GetMapping("/user/{userId}/unread-count-by-type")
    public Result<Map<String, Long>> getUnreadCountByType(
            @PathVariable @NotNull Long userId) {
        Map<String, Long> countMap = notificationService.getUnreadCountByType(userId);
        return Result.success(countMap);
    }

    /**
     * 检查用户是否在线
     */
    @GetMapping("/user/{userId}/online")
    public Result<Boolean> isUserOnline(
            @PathVariable @NotNull Long userId) {
        boolean online = notificationService.isUserOnline(userId);
        return Result.success(online);
    }

    /**
     * 获取在线用户数量
     */
    @GetMapping("/online-count")
    public Result<Integer> getOnlineUserCount() {
        int count = notificationService.getOnlineUserCount();
        return Result.success(count);
    }

    /**
     * 广播系统通知
     */
    @PostMapping("/broadcast")
    public Result<Void> broadcastSystemNotification(
            @RequestParam String title,
            @RequestParam String content) {
        notificationService.broadcastSystemNotification(title, content);
        return Result.success();
    }

    /**
     * 获取通知统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getNotificationStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Map<String, Object> statistics = notificationService.getNotificationStatistics(startTime, endTime);
        return Result.success(statistics);
    }

    /**
     * 处理未推送的通知（定时任务调用）
     */
    @PostMapping("/handle-unpushed")
    public Result<Void> handleUnpushedNotifications() {
        notificationService.handleUnpushedNotifications();
        return Result.success();
    }

    /**
     * 清理过期通知（定时任务调用）
     */
    @PostMapping("/clean-expired")
    public Result<Void> cleanExpiredNotifications() {
        notificationService.cleanExpiredNotifications();
        return Result.success();
    }
}