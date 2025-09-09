package com.sharedroom.payment.listener;

import com.sharedroom.payment.feign.OrderFeignClient;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 支付成功消息监听器
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = "payment-success-topic",
    consumerGroup = "payment-success-consumer-group"
)
public class PaymentSuccessListener implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(PaymentSuccessListener.class);

    private final OrderFeignClient orderFeignClient;

    @Override
    public void onMessage(String orderId) {
        try {
            log.info("收到支付成功消息，订单ID: {}", orderId);
            
            // 调用订单服务更新订单状态为已支付
            Long orderIdLong = Long.valueOf(orderId);
            orderFeignClient.payOrder(orderIdLong, "ALIPAY", "SYSTEM_AUTO_PAY");
            
            log.info("订单支付状态更新成功，订单ID: {}", orderId);
        } catch (Exception e) {
            log.error("处理支付成功消息失败，订单ID: {}", orderId, e);
            throw e; // 重新抛出异常，触发消息重试
        }
    }
}