package com.sharedroom.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharedroom.common.entity.Payment;
import com.sharedroom.common.exception.BusinessException;
import com.sharedroom.common.result.Result;
import com.sharedroom.common.result.ResultCode;
import com.sharedroom.common.utils.UserContext;
import com.sharedroom.payment.dto.PaymentDTO;
import com.sharedroom.payment.feign.OrderFeignClient;
import com.sharedroom.payment.mapper.PaymentMapper;
import com.sharedroom.payment.service.PaymentService;
import com.sharedroom.payment.vo.OrderVO;
import com.sharedroom.payment.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 支付服务实现类
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentMapper paymentMapper;
    private final OrderFeignClient orderFeignClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RocketMQTemplate rocketMQTemplate;

    // 支付宝配置
    @Value("${payment.alipay.app-id}")
    private String alipayAppId;
    
    @Value("${payment.alipay.private-key}")
    private String alipayPrivateKey;
    
    @Value("${payment.alipay.public-key}")
    private String alipayPublicKey;
    
    @Value("${payment.alipay.gateway-url}")
    private String alipayGatewayUrl;
    
    @Value("${payment.alipay.notify-url}")
    private String alipayNotifyUrl;

    private static final int PAYMENT_EXPIRE_MINUTES = 15; // 支付过期时间15分钟
    private static final String PAYMENT_LOCK_KEY = "payment:lock:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO createPayment(PaymentDTO paymentDTO) {
        String lockKey = PAYMENT_LOCK_KEY + paymentDTO.getOrderId();
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
        
        if (!Boolean.TRUE.equals(lockAcquired)) {
            throw new BusinessException(ResultCode.SYSTEM_BUSY.getCode(), "支付处理中，请稍后重试");
        }
        
        try {
            // 1. 验证订单信息
            Result<OrderVO> orderResult = orderFeignClient.getOrderById(paymentDTO.getOrderId());
            if (!orderResult.isSuccess() || orderResult.getData() == null) {
                throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
            }
            
            OrderVO order = orderResult.getData();
            if (!order.getStatus().equals(1)) { // 只有待支付订单才能支付
                throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
            }

            // 2. 检查是否已有支付记录
            List<Payment> existingPayments = paymentMapper.selectByOrderId(paymentDTO.getOrderId());
            Payment pendingPayment = existingPayments.stream()
                    .filter(p -> p.getStatus().equals(1))
                    .findFirst()
                    .orElse(null);
            
            if (pendingPayment != null) {
                // 如果已有待支付记录，直接返回
                return convertToVO(pendingPayment);
            }

            // 3. 创建支付记录
            Payment payment = buildPayment(paymentDTO, order);
            this.save(payment);

            // 4. 根据支付方式调用相应的支付接口
            PaymentVO paymentVO = convertToVO(payment);
            switch (paymentDTO.getPayMethod().toLowerCase()) {
                case "alipay":
                    return processAlipayPayment(payment, paymentDTO);
                default:
                    throw new BusinessException(ResultCode.PAYMENT_METHOD_NOT_SUPPORTED);
            }
        } finally {
            // 释放分布式锁
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public PaymentVO alipayPay(PaymentDTO paymentDTO) {
        return createPayment(paymentDTO);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleAlipayNotify(Map<String, String> params) {
        try {
            // 1. 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
            if (!signVerified) {
                log.error("支付宝回调签名验证失败: {}", params);
                return false;
            }

            // 2. 获取支付信息
            String outTradeNo = params.get("out_trade_no"); // 我们的支付流水号
            String tradeNo = params.get("trade_no"); // 支付宝交易号
            String tradeStatus = params.get("trade_status");
            String totalAmount = params.get("total_amount");

            // 3. 查询支付记录
            Payment payment = paymentMapper.selectByTransactionId(outTradeNo);
            if (payment == null) {
                log.error("支付记录不存在: {}", outTradeNo);
                return false;
            }

            // 4. 验证金额
            if (!payment.getAmount().equals(new BigDecimal(totalAmount))) {
                log.error("支付金额不匹配: expected={}, actual={}", payment.getAmount(), totalAmount);
                return false;
            }

            // 5. 处理支付结果
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 支付成功
                if (payment.getStatus().equals(1)) { // 只处理待支付状态的订单
                    paymentMapper.updatePaymentSuccess(payment.getId(), tradeNo, LocalDateTime.now(), LocalDateTime.now());
                    
                    // 通知订单服务更新订单状态
                    orderFeignClient.payOrder(payment.getOrderId(), "alipay", outTradeNo);
                    
                    // 发送支付成功消息
                    sendPaymentSuccessMessage(payment.getId());
                    
                    log.info("支付宝支付成功: paymentId={}, tradeNo={}", payment.getId(), tradeNo);
                }
                return true;
            }
            
            return true;
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
            return false;
        }
    }



    @Override
    public PaymentVO queryPaymentStatus(String transactionId) {
        Payment payment = paymentMapper.selectByTransactionId(transactionId);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }
        
        // 如果是待支付状态，查询第三方支付状态
        if (payment.getStatus().equals(1)) {
            syncPaymentStatus(transactionId);
            // 重新查询
            payment = paymentMapper.selectByTransactionId(transactionId);
        }
        
        return convertToVO(payment);
    }

    @Override
    public List<PaymentVO> getPaymentsByOrderId(Long orderId) {
        List<Payment> payments = paymentMapper.selectByOrderId(orderId);
        return payments.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public PaymentVO getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentMapper.selectByTransactionId(transactionId);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }
        return convertToVO(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelPayment(String transactionId) {
        Payment payment = paymentMapper.selectByTransactionId(transactionId);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }
        
        if (!payment.getStatus().equals(1)) {
            throw new BusinessException(ResultCode.PAYMENT_STATUS_ERROR);
        }
        
        // 更新支付状态为已取消
        return paymentMapper.updatePaymentStatus(payment.getId(), 5, LocalDateTime.now()) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundPayment(String transactionId, BigDecimal refundAmount, String refundReason) {
        Payment payment = paymentMapper.selectByTransactionId(transactionId);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }
        
        if (!payment.getStatus().equals(2)) {
            throw new BusinessException(ResultCode.PAYMENT_STATUS_ERROR);
        }
        
        // 这里应该调用第三方退款接口
        // 简化处理，直接更新状态
        boolean success = paymentMapper.updatePaymentStatus(payment.getId(), 4, LocalDateTime.now()) > 0;
        
        if (success) {
            log.info("退款成功: paymentId={}, refundAmount={}", payment.getId(), refundAmount);
        }
        
        return success;
    }

    @Override
    public Page<PaymentVO> getUserPayments(Page<Payment> page, Long userId, Integer status) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getUserId, userId)
                .eq(status != null, Payment::getStatus, status)
                .orderByDesc(Payment::getCreateTime);
        
        Page<Payment> paymentPage = this.page(page, wrapper);
        
        // 转换为VO
        Page<PaymentVO> voPage = new Page<>();
        BeanUtils.copyProperties(paymentPage, voPage);
        List<PaymentVO> voList = paymentPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public Page<PaymentVO> getCurrentUserPayments(Page<Payment> page, Integer status) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        return getUserPayments(page, userId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleExpiredPayments() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(PAYMENT_EXPIRE_MINUTES);
        List<Payment> expiredPayments = paymentMapper.selectExpiredPayments(expireTime);
        
        for (Payment payment : expiredPayments) {
            // 取消过期支付
            paymentMapper.updatePaymentStatus(payment.getId(), 5, LocalDateTime.now());
            
            // 取消对应订单
            orderFeignClient.cancelOrder(payment.getOrderId(), "支付超时");
            
            log.info("处理过期支付: paymentId={}", payment.getId());
        }
    }

    @Override
    public Map<String, Object> getPaymentStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 统计支付金额
        BigDecimal totalAmount = paymentMapper.sumPaymentAmount(startTime, endTime);
        statistics.put("totalAmount", totalAmount);
        
        // 统计支付笔数
        Long totalCount = paymentMapper.countPayments(startTime, endTime);
        statistics.put("totalCount", totalCount);
        
        return statistics;
    }

    @Override
    public boolean verifyPaymentSign(Map<String, String> params, String payMethod) {
        try {
            if ("alipay".equals(payMethod)) {
                return AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
            }
            return false;
        } catch (Exception e) {
            log.error("验证支付签名异常", e);
            return false;
        }
    }

    @Override
    public boolean syncPaymentStatus(String transactionId) {
        Payment payment = paymentMapper.selectByTransactionId(transactionId);
        if (payment == null) {
            return false;
        }
        
        try {
            if ("alipay".equals(payment.getPayMethod())) {
                return syncAlipayStatus(payment);
            }
            return false;
        } catch (Exception e) {
            log.error("同步支付状态异常: transactionId={}", transactionId, e);
            return false;
        }
    }

    /**
     * 处理支付宝支付
     */
    private PaymentVO processAlipayPayment(Payment payment, PaymentDTO paymentDTO) {
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(alipayGatewayUrl, alipayAppId, alipayPrivateKey, "json", "UTF-8", alipayPublicKey, "RSA2");
            
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            request.setNotifyUrl(alipayNotifyUrl);
            
            AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
            model.setOutTradeNo(payment.getTransactionId());
            model.setTotalAmount(payment.getAmount().toString());
            model.setSubject(paymentDTO.getDescription() != null ? paymentDTO.getDescription() : "共享自习室座位费用");
            
            request.setBizModel(model);
            
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            
            PaymentVO paymentVO = convertToVO(payment);
            if (response.isSuccess()) {
                paymentVO.setQrCodeUrl(response.getQrCode());
                log.info("支付宝预下单成功: paymentId={}, qrCode={}", payment.getId(), response.getQrCode());
            } else {
                log.error("支付宝预下单失败: {}", response.getSubMsg());
                throw new BusinessException(ResultCode.PAYMENT_CREATE_FAILED);
            }
            
            return paymentVO;
        } catch (AlipayApiException e) {
            log.error("支付宝支付异常", e);
            throw new BusinessException(ResultCode.PAYMENT_CREATE_FAILED);
        }
    }



    /**
     * 同步支付宝支付状态
     */
    private boolean syncAlipayStatus(Payment payment) {
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(alipayGatewayUrl, alipayAppId, alipayPrivateKey, "json", "UTF-8", alipayPublicKey, "RSA2");
            
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{\"out_trade_no\":\"" + payment.getTransactionId() + "\"}");
            
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 更新为支付成功
                    paymentMapper.updatePaymentSuccess(payment.getId(), response.getTradeNo(), LocalDateTime.now(), LocalDateTime.now());
                    return true;
                }
            }
            
            return false;
        } catch (AlipayApiException e) {
            log.error("同步支付宝状态异常", e);
            return false;
        }
    }



    /**
     * 构建支付对象
     */
    private Payment buildPayment(PaymentDTO paymentDTO, OrderVO order) {
        Payment payment = new Payment();
        payment.setTransactionId(generateTransactionId());
        payment.setOrderId(paymentDTO.getOrderId());
        payment.setUserId(order.getUserId());
        payment.setAmount(paymentDTO.getAmount());
        payment.setPayMethod(paymentDTO.getPayMethod());
        payment.setStatus(1); // 待支付
        payment.setDescription(paymentDTO.getDescription());
        return payment;
    }

    /**
     * 生成支付流水号
     */
    private String generateTransactionId() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 发送支付成功消息
     */
    private void sendPaymentSuccessMessage(Long paymentId) {
        try {
            rocketMQTemplate.syncSend("payment-success-topic", paymentId);
        } catch (Exception e) {
            log.error("发送支付成功消息失败: paymentId={}", paymentId, e);
        }
    }

    /**
     * 转换为VO对象
     */
    private PaymentVO convertToVO(Payment payment) {
        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(payment, vo);
        
        // 设置支付方式名称
        vo.setPayMethodName(getPayMethodName(payment.getPayMethod()));
        
        // 设置状态名称
        vo.setStatusName(getPaymentStatusName(payment.getStatus()));
        
        // 设置操作权限
        vo.setCanCancel(payment.getStatus() == 1);
        vo.setCanRefund(payment.getStatus() == 2);
        
        // 计算剩余支付时间
        if (payment.getStatus() == 1) {
            LocalDateTime expireTime = payment.getCreateTime().plusMinutes(PAYMENT_EXPIRE_MINUTES);
            long remainingMinutes = java.time.Duration.between(LocalDateTime.now(), expireTime).toMinutes();
            vo.setRemainingPayTime(Math.max(0, remainingMinutes));
        }
        
        return vo;
    }

    /**
     * 获取支付方式名称
     */
    private String getPayMethodName(String payMethod) {
        if (payMethod == null) return "未知";
        switch (payMethod.toLowerCase()) {
            case "alipay": return "支付宝";
            default: return "未知";
        }
    }

    /**
     * 获取支付状态名称
     */
    private String getPaymentStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "待支付";
            case 2: return "已支付";
            case 3: return "支付失败";
            case 4: return "已退款";
            case 5: return "已取消";
            default: return "未知";
        }
    }
}