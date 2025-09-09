package com.sharedroom.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sharedroom.common.entity.Order;
import com.sharedroom.order.dto.CreateOrderDTO;
import com.sharedroom.order.vo.OrderVO;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     */
    OrderVO createOrder(CreateOrderDTO createOrderDTO);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId, String cancelReason);

    /**
     * 支付订单
     */
    boolean payOrder(Long orderId, String payMethod, String payTransactionId);

    /**
     * 完成订单
     */
    boolean completeOrder(Long orderId);

    /**
     * 退款订单
     */
    boolean refundOrder(Long orderId, String refundReason);

    /**
     * 根据订单ID获取订单详情
     */
    OrderVO getOrderById(Long orderId);

    /**
     * 根据订单号获取订单详情
     */
    OrderVO getOrderByOrderNo(String orderNo);

    /**
     * 获取当前用户的订单列表
     */
    Page<OrderVO> getCurrentUserOrders(Page<Order> page, Integer status);

    /**
     * 获取用户的订单列表
     */
    List<OrderVO> getUserOrders(Long userId, Integer status);

    /**
     * 处理超时未支付的订单
     */
    void handleExpiredOrders();

    /**
     * 检查用户在指定时间段是否有冲突订单
     */
    boolean hasConflictOrder(Long userId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

    /**
     * 开始使用订单(用户到达自习室)
     */
    boolean startUsingOrder(Long orderId);

    /**
     * 获取订单统计信息
     */
    Object getOrderStatistics();
}