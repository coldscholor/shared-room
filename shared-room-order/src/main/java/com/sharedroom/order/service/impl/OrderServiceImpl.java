package com.sharedroom.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharedroom.common.entity.Order;
import com.sharedroom.common.exception.BusinessException;
import com.sharedroom.common.result.Result;
import com.sharedroom.common.result.ResultCode;
import com.sharedroom.common.utils.UserContext;
import com.sharedroom.order.dto.CreateOrderDTO;
import com.sharedroom.order.feign.SeatFeignClient;
import com.sharedroom.order.mapper.OrderMapper;
import com.sharedroom.order.service.OrderService;
import com.sharedroom.order.vo.OrderVO;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderMapper orderMapper;
    private final SeatFeignClient seatFeignClient;
    private final RocketMQTemplate rocketMQTemplate;

    private static final int ORDER_EXPIRE_MINUTES = 15; // 订单过期时间15分钟

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderDTO createOrderDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }

        // 1. 验证时间参数
        validateOrderTime(createOrderDTO);

        // 2. 检查用户是否有冲突订单
        if (hasConflictOrder(userId, createOrderDTO.getStartTime(), createOrderDTO.getEndTime())) {
            throw new BusinessException(ResultCode.ORDER_TIME_CONFLICT);
        }

        // 3. 检查座位是否可用
        Result<Boolean> seatResult = seatFeignClient.checkSeatAvailable(createOrderDTO.getSeatId());
        if (!seatResult.isSuccess() || !Boolean.TRUE.equals(seatResult.getData())) {
            throw new BusinessException(ResultCode.SEAT_NOT_AVAILABLE);
        }

        // 4. 预订座位
        Result<Boolean> reserveResult = seatFeignClient.reserveSeat(createOrderDTO.getSeatId());
        if (!reserveResult.isSuccess() || !Boolean.TRUE.equals(reserveResult.getData())) {
            throw new BusinessException(ResultCode.SEAT_RESERVE_FAILED);
        }

        try {
            // 5. 创建订单
            Order order = buildOrder(createOrderDTO, userId);
            this.save(order);

            // 6. 发送延时消息，处理订单超时
            sendOrderExpireMessage(order.getId());

            log.info("订单创建成功: orderId={}, userId={}, seatId={}", order.getId(), userId, createOrderDTO.getSeatId());
            return convertToVO(order);
        } catch (Exception e) {
            // 如果订单创建失败，释放座位
            seatFeignClient.releaseSeat(createOrderDTO.getSeatId());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, String cancelReason) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 检查订单状态
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 检查是否是订单所有者
        Long userId = UserContext.getUserId();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_BELONG_TO_USER);
        }

        // 更新订单状态
        order.setStatus(5); // 已取消
        order.setCancelReason(cancelReason);
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        boolean updated = this.updateById(order);
        if (updated) {
            // 释放座位
            seatFeignClient.releaseSeat(order.getSeatId());
            log.info("订单取消成功: orderId={}, userId={}", orderId, userId);
        }
        
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId, String payMethod, String payTransactionId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 检查订单状态
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 检查订单是否过期
        if (isOrderExpired(order)) {
            throw new BusinessException(ResultCode.ORDER_EXPIRED);
        }

        // 更新支付信息
        int updated = orderMapper.updatePaymentInfo(orderId, 2, payMethod, LocalDateTime.now(), payTransactionId);
        if (updated > 0) {
            log.info("订单支付成功: orderId={}, payMethod={}", orderId, payMethod);
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 检查订单状态
        if (order.getStatus() != 3) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 更新订单状态为已完成
        boolean updated = orderMapper.updateOrderStatus(orderId, 4) > 0;
        if (updated) {
            // 释放座位
            seatFeignClient.releaseSeat(order.getSeatId());
            log.info("订单完成: orderId={}", orderId);
        }
        
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundOrder(Long orderId, String refundReason) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 检查订单状态
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 更新订单状态为已退款
        boolean updated = orderMapper.updateOrderStatus(orderId, 6) > 0;
        if (updated) {
            // 释放座位
            seatFeignClient.releaseSeat(order.getSeatId());
            log.info("订单退款成功: orderId={}, refundReason={}", orderId, refundReason);
        }
        
        return updated;
    }

    @Override
    public OrderVO getOrderById(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return convertToVO(order);
    }

    @Override
    public OrderVO getOrderByOrderNo(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return convertToVO(order);
    }

    @Override
    public Page<OrderVO> getCurrentUserOrders(Page<Order> page, Integer status) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);
        
        Page<Order> orderPage = this.page(page, wrapper);
        
        // 转换为VO
        Page<OrderVO> voPage = new Page<>();
        BeanUtils.copyProperties(orderPage, voPage);
        List<OrderVO> voList = orderPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public List<OrderVO> getUserOrders(Long userId, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);
        
        List<Order> orders = this.list(wrapper);
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleExpiredOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(ORDER_EXPIRE_MINUTES);
        List<Order> expiredOrders = orderMapper.selectExpiredOrders(expireTime);
        
        for (Order order : expiredOrders) {
            // 取消过期订单
            order.setStatus(5); // 已取消
            order.setCancelReason("订单超时未支付");
            order.setCancelTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            this.updateById(order);
            
            // 释放座位
            seatFeignClient.releaseSeat(order.getSeatId());
            
            log.info("处理过期订单: orderId={}", order.getId());
        }
    }

    @Override
    public boolean hasConflictOrder(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Order> conflictOrders = orderMapper.selectConflictOrders(userId, startTime, endTime);
        return !conflictOrders.isEmpty();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startUsingOrder(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 检查订单状态
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 更新订单状态为使用中
        boolean updated = orderMapper.updateOrderStatus(orderId, 3) > 0;
        if (updated) {
            log.info("开始使用订单: orderId={}", orderId);
        }
        
        return updated;
    }

    @Override
    public Object getOrderStatistics() {
        // 这里可以实现订单统计逻辑
        return null;
    }

    /**
     * 验证订单时间
     */
    private void validateOrderTime(CreateOrderDTO createOrderDTO) {
        LocalDateTime now = LocalDateTime.now();
        
        if (createOrderDTO.getStartTime().isBefore(now)) {
            throw new BusinessException(ResultCode.ORDER_START_TIME_ERROR);
        }
        
        if (createOrderDTO.getEndTime().isBefore(createOrderDTO.getStartTime())) {
            throw new BusinessException(ResultCode.ORDER_END_TIME_ERROR);
        }
        
        // 计算时长
        Duration duration = Duration.between(createOrderDTO.getStartTime(), createOrderDTO.getEndTime());
        long hours = duration.toHours();
        
        if (hours <= 0 || hours > 24) {
            throw new BusinessException(ResultCode.ORDER_DURATION_ERROR);
        }
        
        createOrderDTO.setDuration((int) hours);
    }

    /**
     * 构建订单对象
     */
    private Order buildOrder(CreateOrderDTO createOrderDTO, Long userId) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setSeatId(createOrderDTO.getSeatId());
        order.setStartTime(createOrderDTO.getStartTime());
        order.setEndTime(createOrderDTO.getEndTime());
        order.setDuration(createOrderDTO.getDuration());
        order.setStatus(1); // 待支付
        order.setRemark(createOrderDTO.getRemark());
        
        // 这里应该根据座位价格计算订单金额
        // 暂时设置固定价格
        BigDecimal hourlyPrice = new BigDecimal("10.00");
        BigDecimal totalAmount = hourlyPrice.multiply(new BigDecimal(createOrderDTO.getDuration()));
        order.setTotalAmount(totalAmount);
        order.setActualAmount(totalAmount);
        
        return order;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 检查订单是否过期
     */
    private boolean isOrderExpired(Order order) {
        return order.getCreateTime().plusMinutes(ORDER_EXPIRE_MINUTES).isBefore(LocalDateTime.now());
    }

    /**
     * 发送订单过期消息
     */
    private void sendOrderExpireMessage(Long orderId) {
        try {
            // 发送延时消息，15分钟后处理订单过期
            rocketMQTemplate.syncSend("order-expire-topic", orderId);
        } catch (Exception e) {
            log.error("发送订单过期消息失败: orderId={}", orderId, e);
        }
    }

    /**
     * 转换为VO对象
     */
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        
        // 设置状态名称
        vo.setStatusName(getOrderStatusName(order.getStatus()));
        
        // 设置操作权限
        vo.setCanCancel(order.getStatus() == 1 || order.getStatus() == 2);
        vo.setCanRefund(order.getStatus() == 2);
        
        // 计算剩余支付时间
        if (order.getStatus() == 1) {
            LocalDateTime expireTime = order.getCreateTime().plusMinutes(ORDER_EXPIRE_MINUTES);
            Duration remaining = Duration.between(LocalDateTime.now(), expireTime);
            vo.setRemainingPayTime(Math.max(0, remaining.toMinutes()));
        }
        
        return vo;
    }

    /**
     * 获取订单状态名称
     */
    private String getOrderStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "待支付";
            case 2: return "已支付";
            case 3: return "使用中";
            case 4: return "已完成";
            case 5: return "已取消";
            case 6: return "已退款";
            default: return "未知";
        }
    }
}