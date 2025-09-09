package com.sharedroom.order.listener;

import com.sharedroom.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 订单过期消息监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "order-expire-topic",
        consumerGroup = "order-expire-consumer-group"
)
public class OrderExpireListener implements RocketMQListener<Long> {

    private final OrderService orderService;

    @Override
    public void onMessage(Long orderId) {
        try {
            log.info("收到订单过期消息: orderId={}", orderId);
            
            // 检查订单状态并处理过期订单
            // 这里可以添加更精确的过期处理逻辑
            orderService.handleExpiredOrders();
            
            log.info("订单过期处理完成: orderId={}", orderId);
        } catch (Exception e) {
            log.error("处理订单过期消息失败: orderId={}", orderId, e);
            // 这里可以添加重试逻辑或者发送到死信队列
        }
    }
}