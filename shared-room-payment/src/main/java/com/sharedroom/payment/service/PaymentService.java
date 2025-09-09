package com.sharedroom.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sharedroom.common.entity.Payment;
import com.sharedroom.payment.dto.PaymentDTO;
import com.sharedroom.payment.vo.PaymentVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 支付服务接口
 */
public interface PaymentService extends IService<Payment> {

    /**
     * 创建支付订单
     */
    PaymentVO createPayment(PaymentDTO paymentDTO);

    /**
     * 支付宝支付
     */
    PaymentVO alipayPay(PaymentDTO paymentDTO);

    /**
     * 处理支付宝回调
     */
    boolean handleAlipayNotify(Map<String, String> params);

    /**
     * 查询支付状态
     */
    PaymentVO queryPaymentStatus(String transactionId);

    /**
     * 根据订单ID查询支付记录
     */
    List<PaymentVO> getPaymentsByOrderId(Long orderId);

    /**
     * 根据支付流水号查询支付记录
     */
    PaymentVO getPaymentByTransactionId(String transactionId);

    /**
     * 取消支付
     */
    boolean cancelPayment(String transactionId);

    /**
     * 退款
     */
    boolean refundPayment(String transactionId, BigDecimal refundAmount, String refundReason);

    /**
     * 获取用户支付记录（分页）
     */
    Page<PaymentVO> getUserPayments(Page<Payment> page, Long userId, Integer status);

    /**
     * 获取当前用户支付记录（分页）
     */
    Page<PaymentVO> getCurrentUserPayments(Page<Payment> page, Integer status);

    /**
     * 处理超时支付
     */
    void handleExpiredPayments();

    /**
     * 获取支付统计信息
     */
    Map<String, Object> getPaymentStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 验证支付签名
     */
    boolean verifyPaymentSign(Map<String, String> params, String payMethod);

    /**
     * 同步支付状态
     */
    boolean syncPaymentStatus(String transactionId);
}