package com.sharedroom.notification.listener;

import com.alibaba.fastjson.JSON;
import com.sharedroom.notification.dto.NotificationDTO;
import com.sharedroom.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通知消息监听器
 * 监听各种业务事件，发送相应通知
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "notification-topic",
        consumerGroup = "notification-consumer-group"
)
public class NotificationListener implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private final NotificationService notificationService;

    @Override
    public void onMessage(String message) {
        try {
            log.info("收到通知消息: {}", message);
            
            // 解析消息
            Map<String, Object> messageMap = JSON.parseObject(message, Map.class);
            String eventType = (String) messageMap.get("eventType");
            Map<String, Object> data = (Map<String, Object>) messageMap.get("data");
            
            // 根据事件类型处理不同的通知
            switch (eventType) {
                case "ORDER_CREATED":
                    handleOrderCreated(data);
                    break;
                case "ORDER_PAID":
                    handleOrderPaid(data);
                    break;
                case "ORDER_CANCELLED":
                    handleOrderCancelled(data);
                    break;
                case "SEAT_RESERVED":
                    handleSeatReserved(data);
                    break;
                case "SEAT_RELEASED":
                    handleSeatReleased(data);
                    break;
                case "PAYMENT_SUCCESS":
                    handlePaymentSuccess(data);
                    break;
                case "PAYMENT_FAILED":
                    handlePaymentFailed(data);
                    break;
                case "USER_REGISTERED":
                    handleUserRegistered(data);
                    break;
                case "SYSTEM_MAINTENANCE":
                    handleSystemMaintenance(data);
                    break;
                default:
                    log.warn("未知的事件类型: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("处理通知消息失败: {}", message, e);
            throw e; // 重新抛出异常，触发重试机制
        }
    }

    /**
     * 处理订单创建事件
     */
    private void handleOrderCreated(Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        String orderNo = (String) data.get("orderNo");
        String seatName = (String) data.get("seatName");
        
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(userId);
        notification.setType(1); // 1表示订单类型
        notification.setTitle("订单创建成功");
        notification.setContent(String.format("您的订单 %s 已创建成功，座位：%s，请及时完成支付。", orderNo, seatName));
        notification.setBusinessId(Long.valueOf(orderNo));
        notification.setBusinessType("ORDER");
        notification.setIsPush(true);
        
        notificationService.sendNotification(notification);
    }

    /**
     * 处理订单支付成功事件
     */
    private void handleOrderPaid(Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        String orderNo = (String) data.get("orderNo");
        String seatName = (String) data.get("seatName");
        
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(userId);
        notification.setType(1); // 1表示订单类型
        notification.setTitle("支付成功");
        notification.setContent(String.format("订单 %s 支付成功，座位 %s 已为您预留，请按时使用。", orderNo, seatName));
        notification.setBusinessId(Long.valueOf(orderNo));
        notification.setBusinessType("ORDER");
        notification.setIsPush(true);
        
        notificationService.sendNotification(notification);
    }

    /**
     * 处理订单取消事件
     */
    private void handleOrderCancelled(Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        String orderNo = (String) data.get("orderNo");
        String reason = (String) data.get("reason");
        
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(userId);
        notification.setType(1); // 1表示订单类型
        notification.setTitle("订单已取消");
        notification.setContent(String.format("您的订单 %s 已取消，原因：%s", orderNo, reason));
        notification.setBusinessId(Long.valueOf(orderNo));
        notification.setBusinessType("ORDER");
        notification.setIsPush(true);
        
        notificationService.sendNotification(notification);
    }

    /**
     * 处理座位预订事件
     */
    private void handleSeatReserved(Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        String seatName = (String) data.get("seatName");
        String startTime = (String) data.get("startTime");
        String endTime = (String) data.get("endTime");
        
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(userId);
        notification.setType(2); // 2表示座位类型
        notification.setTitle("座位预订成功");
        notification.setContent(String.format("座位 %s 预订成功，使用时间：%s - %s", seatName, startTime, endTime));
        notification.setBusinessId(Long.valueOf(seatName.hashCode()));
        notification.setBusinessType("SEAT");
        notification.setIsPush(true);
        
        notificationService.sendNotification(notification);
    }

    /**
     * 处理座位释放事件
     */
    private void handleSeatReleased(Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        String seatName = (String) data.get("seatName");
        
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(userId);
        notification.setType(2); // 2表示座位类型
        notification.setTitle("座位已释放");
        notification.setContent(String.format("您的座位 %s 使用时间已到，座位已自动释放。", seatName));
        notification.setBusinessId((long) seatName.hashCode());
        notification.setBusinessType("SEAT");
        notification.setIsPush(true);
        
        notificationService.sendNotification(notification);
    }

    /**
     * 处理支付成功事件
     */
    private void handlePaymentSuccess(Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        String paymentNo = (String) data.get("paymentNo");
        String amount = data.get("amount").toString();
        
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(userId);
        notification.setType(3); // 3表示支付类型
        notification.setTitle("支付成功");
        notification.setContent(String.format("支付成功，支付流水号：%s，金额：￥%s", paymentNo, amount));
        notification.setBusinessId(Long.valueOf(paymentNo.hashCode()));
        notification.setBusinessType("PAYMENT");
        notification.setIsPush(true);
        
        notificationService.sendNotification(notification);
    }

    /**
     * 处理支付失败事件
     */
    private void handlePaymentFailed(Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        String paymentNo = (String) data.get("paymentNo");
        String reason = (String) data.get("reason");
        
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(userId);
        notification.setType(3); // 3表示支付类型
        notification.setTitle("支付失败");
        notification.setContent(String.format("支付失败，支付流水号：%s，失败原因：%s", paymentNo, reason));
        notification.setBusinessId((long) paymentNo.hashCode());
        notification.setBusinessType("PAYMENT");
        notification.setIsPush(true);
        
        notificationService.sendNotification(notification);
    }

    /**
     * 处理用户注册事件
     */
    private void handleUserRegistered(Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        String username = (String) data.get("username");
        String email = (String) data.get("email");
        
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(userId);
        notification.setType(4); // 4表示系统类型
        notification.setTitle("欢迎注册");
        notification.setContent(String.format("欢迎 %s 注册共享自习室，开始您的学习之旅吧！", username));
        notification.setBusinessId(userId);
        notification.setBusinessType("USER");
        notification.setIsPush(true);
        notification.setEmail(email);
        
        notificationService.sendNotification(notification);
    }

    /**
     * 处理系统维护事件
     */
    private void handleSystemMaintenance(Map<String, Object> data) {
        String title = (String) data.get("title");
        String content = (String) data.get("content");
        String startTime = (String) data.get("startTime");
        String endTime = (String) data.get("endTime");
        
        NotificationDTO notification = new NotificationDTO();
        notification.setType(1); // 1表示系统类型
        notification.setTitle(title);
        notification.setContent(String.format("%s\n维护时间：%s - %s", content, startTime, endTime));
        notification.setBusinessType("SYSTEM");
        notification.setIsPush(true);
        
        // 系统通知广播给所有在线用户
        notificationService.broadcastSystemNotification(notification.getTitle(), notification.getContent());
    }
}