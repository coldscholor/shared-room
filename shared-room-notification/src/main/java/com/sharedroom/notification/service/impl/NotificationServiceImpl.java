package com.sharedroom.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sharedroom.common.utils.UserContext;
import com.sharedroom.notification.entity.Notification;
import com.sharedroom.common.exception.BusinessException;
import com.sharedroom.notification.dto.NotificationDTO;
import com.sharedroom.notification.handler.WebSocketHandler;
import com.sharedroom.notification.mapper.NotificationMapper;
import com.sharedroom.notification.service.NotificationService;
import com.sharedroom.notification.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationMapper notificationMapper;
    private final WebSocketHandler webSocketHandler;
    private final JavaMailSender mailSender;

    @Override
    @Transactional
    public NotificationVO sendNotification(NotificationDTO notificationDTO) {
        // 构建通知对象
        Notification notification = buildNotification(notificationDTO);
        
        // 保存通知记录
        notificationMapper.insert(notification);
        
        // 转换为VO
        NotificationVO notificationVO = convertToVO(notification);
        
        // 异步推送通知
        if (notificationDTO.getImmediate()) {
            asyncPushNotification(notificationDTO, notificationVO);
        }
        
        return notificationVO;
    }

    @Override
    @Transactional
    public List<NotificationVO> batchSendNotification(List<NotificationDTO> notificationDTOs) {
        if (CollectionUtils.isEmpty(notificationDTOs)) {
            return Collections.emptyList();
        }
        
        List<NotificationVO> results = new ArrayList<>();
        for (NotificationDTO dto : notificationDTOs) {
            try {
                NotificationVO vo = sendNotification(dto);
                results.add(vo);
            } catch (Exception e) {
                log.error("批量发送通知失败，用户ID: {}", dto.getUserId(), e);
            }
        }
        
        return results;
    }

    @Override
    public NotificationVO getNotificationById(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        return convertToVO(notification);
    }

    @Override
    public Page<NotificationVO> getUserNotifications(Page<Notification> page, Long userId, Integer type, Boolean isRead) {
        Page<Notification> notificationPage;
        
        if (type != null && isRead != null) {
            notificationPage = notificationMapper.selectByUserIdAndTypeAndReadStatus(page, userId, type, isRead);
        } else if (type != null) {
            notificationPage = notificationMapper.selectByUserIdAndType(page, userId, type);
        } else if (isRead != null) {
            notificationPage = notificationMapper.selectByUserIdAndReadStatus(page, userId, isRead);
        } else {
            notificationPage = notificationMapper.selectByUserId(page, userId);
        }
        
        return convertToVOPage(notificationPage);
    }

    @Override
    public Page<NotificationVO> getCurrentUserNotifications(Page<Notification> page, Integer type, Boolean isRead) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        return getUserNotifications(page, userId, type, isRead);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        
        // 检查权限
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null || !currentUserId.equals(notification.getUserId())) {
            throw new BusinessException("无权限操作此通知");
        }
        
        if (notification.getIsRead()) {
            return true; // 已经是已读状态
        }
        
        notification.setIsRead(true);
        notification.setReadTime(LocalDateTime.now());
        return notificationMapper.updateById(notification) > 0;
    }

    @Override
    @Transactional
    public boolean batchMarkAsRead(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        
        String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        return notificationMapper.batchMarkAsRead(idsStr, userId, LocalDateTime.now()) > 0;
    }

    @Override
    @Transactional
    public boolean markAllAsRead(Long userId) {
        return notificationMapper.markAllAsRead(userId, LocalDateTime.now()) > 0;
    }

    @Override
    @Transactional
    public boolean markCurrentUserAllAsRead() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        return markAllAsRead(userId);
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        
        // 检查权限
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null || !currentUserId.equals(notification.getUserId())) {
            throw new BusinessException("无权限操作此通知");
        }
        
        return notificationMapper.deleteById((java.io.Serializable) id) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeleteNotification(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 检查权限
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Notification::getId, ids)
               .eq(Notification::getUserId, userId);
        
        List<Notification> notifications = notificationMapper.selectList(wrapper);
        if (notifications.size() != ids.size()) {
            throw new BusinessException("部分通知不存在或无权限操作");
        }
        
        return notificationMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    public Long getCurrentUserUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        return getUnreadCount(userId);
    }

    @Override
    public Map<String, Long> getUnreadCountByType(Long userId) {
        List<Map<String, Object>> results = notificationMapper.countUnreadByUserIdGroupByType(userId);
        Map<String, Long> countMap = new HashMap<>();
        
        for (Map<String, Object> result : results) {
            Integer type = (Integer) result.get("type");
            Long count = ((Number) result.get("count")).longValue();
            String typeName = getTypeNameByCode(type);
            countMap.put(typeName, count);
        }
        
        return countMap;
    }

    @Override
    public Map<String, Long> getCurrentUserUnreadCountByType() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        return getUnreadCountByType(userId);
    }

    @Override
    public boolean pushNotificationByWebSocket(Long userId, NotificationVO notification) {
        try {
            Map<String, Object> message = new ConcurrentHashMap<>();
            message.put("type", "notification");
            message.put("data", notification);
            
            return webSocketHandler.pushToUser(userId, message);
        } catch (Exception e) {
            log.error("WebSocket推送通知失败，用户ID: {}", userId, e);
            return false;
        }
    }

    @Override
    @Async
    public boolean pushNotificationByEmail(String email, String title, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(title);
            message.setText(content);
            message.setFrom("noreply@sharedroom.com");
            
            mailSender.send(message);
            log.info("邮件通知发送成功，收件人: {}", email);
            return true;
        } catch (Exception e) {
            log.error("邮件通知发送失败，收件人: {}", email, e);
            return false;
        }
    }

    @Override
    @Async
    public boolean pushNotificationBySms(String phone, String content) {
        try {
            // 这里集成短信服务提供商的SDK
            // 示例代码，实际需要根据具体的短信服务商进行实现
            log.info("短信通知发送成功，手机号: {}, 内容: {}", phone, content);
            return true;
        } catch (Exception e) {
            log.error("短信通知发送失败，手机号: {}", phone, e);
            return false;
        }
    }

    @Override
    public void handleUnpushedNotifications() {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusHours(1); // 处理1小时内的未推送通知
            List<Notification> unpushedNotifications = notificationMapper.selectUnpushedNotifications(startTime, 100);
            
            for (Notification notification : unpushedNotifications) {
                try {
                    NotificationVO vo = convertToVO(notification);
                    boolean success = pushNotificationByWebSocket(notification.getUserId(), vo);
                    
                    if (success) {
                        notificationMapper.updatePushStatus(notification.getId(), LocalDateTime.now());
                    }
                } catch (Exception e) {
                    log.error("处理未推送通知失败，通知ID: {}", notification.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("处理未推送通知异常", e);
        }
    }

    @Override
    public void cleanExpiredNotifications() {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(30); // 删除30天前的通知
            int deletedCount = notificationMapper.deleteExpiredNotifications(expireTime);
            log.info("清理过期通知完成，删除数量: {}", deletedCount);
        } catch (Exception e) {
            log.error("清理过期通知异常", e);
        }
    }

    @Override
    public Map<String, Object> getNotificationStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> statistics = notificationMapper.getNotificationStatistics(startTime, endTime);
        
        Map<String, Object> result = new HashMap<>();
        long totalCount = 0;
        long totalReadCount = 0;
        
        Map<String, Map<String, Object>> typeStatistics = new HashMap<>();
        
        for (Map<String, Object> stat : statistics) {
            Integer type = (Integer) stat.get("type");
            Long count = ((Number) stat.get("total_count")).longValue();
            Long readCount = ((Number) stat.get("read_count")).longValue();
            
            String typeName = getTypeNameByCode(type);
            Map<String, Object> typeStat = new HashMap<>();
            typeStat.put("totalCount", count);
            typeStat.put("readCount", readCount);
            typeStat.put("readRate", count > 0 ? (double) readCount / count : 0.0);
            
            typeStatistics.put(typeName, typeStat);
            
            totalCount += count;
            totalReadCount += readCount;
        }
        
        result.put("totalCount", totalCount);
        result.put("totalReadCount", totalReadCount);
        result.put("totalReadRate", totalCount > 0 ? (double) totalReadCount / totalCount : 0.0);
        result.put("typeStatistics", typeStatistics);
        result.put("onlineUserCount", getOnlineUserCount());
        
        return result;
    }

    @Override
    public int getOnlineUserCount() {
        return webSocketHandler.getOnlineUserCount();
    }

    @Override
    public boolean isUserOnline(Long userId) {
        return webSocketHandler.isUserOnline(userId);
    }

    @Override
    public void broadcastSystemNotification(String title, String content) {
        try {
            Map<String, Object> message = new ConcurrentHashMap<>();
            message.put("type", "system");
            message.put("title", title);
            message.put("content", content);
            message.put("timestamp", System.currentTimeMillis());
            
            webSocketHandler.broadcast(message);
            log.info("系统通知广播成功，标题: {}", title);
        } catch (Exception e) {
            log.error("系统通知广播失败", e);
        }
    }

    /**
     * 异步推送通知
     */
    @Async
    private void asyncPushNotification(NotificationDTO dto, NotificationVO vo) {
        try {
            List<String> channels = dto.getChannels();
            if (CollectionUtils.isEmpty(channels)) {
                channels = Arrays.asList("websocket"); // 默认使用WebSocket推送
            }
            
            boolean pushed = false;
            
            for (String channel : channels) {
                switch (channel.toLowerCase()) {
                    case "websocket":
                        if (pushNotificationByWebSocket(dto.getUserId(), vo)) {
                            pushed = true;
                        }
                        break;
                    case "email":
                        if (StringUtils.hasText(dto.getEmail())) {
                            pushNotificationByEmail(dto.getEmail(), dto.getTitle(), dto.getContent());
                        }
                        break;
                    case "sms":
                        if (StringUtils.hasText(dto.getPhone())) {
                            pushNotificationBySms(dto.getPhone(), dto.getContent());
                        }
                        break;
                }
            }
            
            // 更新推送状态
            if (pushed) {
                notificationMapper.updatePushStatus(vo.getId(), LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("异步推送通知失败", e);
        }
    }

    /**
     * 构建通知对象
     */
    private Notification buildNotification(NotificationDTO dto) {
        Notification notification = new Notification();
        BeanUtils.copyProperties(dto, notification);
        notification.setIsRead(false);
        notification.setIsPushed(false);
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        return notification;
    }

    /**
     * 转换为VO对象
     */
    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        BeanUtils.copyProperties(notification, vo);
        return vo;
    }

    /**
     * 转换为VO分页对象
     */
    private Page<NotificationVO> convertToVOPage(Page<Notification> notificationPage) {
        Page<NotificationVO> voPage = new Page<>();
        BeanUtils.copyProperties(notificationPage, voPage, "records");
        
        List<NotificationVO> voList = notificationPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    /**
     * 根据类型代码获取类型名称
     */
    private String getTypeNameByCode(Integer type) {
        if (type == null) {
            return "未知";
        }
        switch (type) {
            case 1:
                return "系统通知";
            case 2:
                return "订单通知";
            case 3:
                return "支付通知";
            case 4:
                return "座位通知";
            default:
                return "其他";
        }
    }
}