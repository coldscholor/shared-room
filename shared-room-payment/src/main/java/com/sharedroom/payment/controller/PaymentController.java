package com.sharedroom.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sharedroom.common.entity.Payment;
import com.sharedroom.common.result.Result;
import com.sharedroom.payment.dto.PaymentDTO;
import com.sharedroom.payment.service.PaymentService;
import com.sharedroom.payment.vo.PaymentVO;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    /**
     * 创建支付订单
     */
    @PostMapping
    public Result<PaymentVO> createPayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        PaymentVO paymentVO = paymentService.createPayment(paymentDTO);
        return Result.success(paymentVO);
    }

    /**
     * 支付宝支付
     */
    @PostMapping("/alipay")
    public Result<PaymentVO> alipayPay(@Valid @RequestBody PaymentDTO paymentDTO) {
        paymentDTO.setPayMethod("alipay");
        PaymentVO paymentVO = paymentService.alipayPay(paymentDTO);
        return Result.success(paymentVO);
    }



    /**
     * 支付宝回调
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            // 获取支付宝回调参数
            Map<String, String> params = new HashMap<>();
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String paramValue = request.getParameter(paramName);
                params.put(paramName, paramValue);
            }
            
            log.info("收到支付宝回调: {}", params);
            
            boolean success = paymentService.handleAlipayNotify(params);
            return success ? "success" : "fail";
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
            return "fail";
        }
    }



    /**
     * 查询支付状态
     */
    @GetMapping("/status/{transactionId}")
    public Result<PaymentVO> queryPaymentStatus(
            @PathVariable @NotBlank String transactionId) {
        PaymentVO paymentVO = paymentService.queryPaymentStatus(transactionId);
        return Result.success(paymentVO);
    }

    /**
     * 根据支付流水号获取支付详情
     */
    @GetMapping("/{transactionId}")
    public Result<PaymentVO> getPaymentByTransactionId(
            @PathVariable @NotBlank String transactionId) {
        PaymentVO paymentVO = paymentService.getPaymentByTransactionId(transactionId);
        return Result.success(paymentVO);
    }

    /**
     * 根据订单ID获取支付记录
     */
    @GetMapping("/order/{orderId}")
    public Result<List<PaymentVO>> getPaymentsByOrderId(
            @PathVariable @NotNull Long orderId) {
        List<PaymentVO> payments = paymentService.getPaymentsByOrderId(orderId);
        return Result.success(payments);
    }

    /**
     * 获取当前用户支付记录（分页）
     */
    @GetMapping("/my")
    public Result<Page<PaymentVO>> getCurrentUserPayments(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<Payment> page = new Page<>(current, size);
        Page<PaymentVO> paymentPage = paymentService.getCurrentUserPayments(page, status);
        return Result.success(paymentPage);
    }

    /**
     * 获取指定用户支付记录（分页）
     */
    @GetMapping("/user/{userId}")
    public Result<Page<PaymentVO>> getUserPayments(
            @PathVariable @NotNull Long userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<Payment> page = new Page<>(current, size);
        Page<PaymentVO> paymentPage = paymentService.getUserPayments(page, userId, status);
        return Result.success(paymentPage);
    }

    /**
     * 取消支付
     */
    @PutMapping("/{transactionId}/cancel")
    public Result<Boolean> cancelPayment(
            @PathVariable @NotBlank String transactionId) {
        boolean success = paymentService.cancelPayment(transactionId);
        return Result.success(success);
    }

    /**
     * 退款
     */
    @PutMapping("/{transactionId}/refund")
    public Result<Boolean> refundPayment(
            @PathVariable @NotBlank String transactionId,
            @RequestParam @NotNull BigDecimal refundAmount,
            @RequestParam(required = false) String refundReason) {
        boolean success = paymentService.refundPayment(transactionId, refundAmount, refundReason);
        return Result.success(success);
    }

    /**
     * 同步支付状态
     */
    @PutMapping("/{transactionId}/sync")
    public Result<Boolean> syncPaymentStatus(
            @PathVariable @NotBlank String transactionId) {
        boolean success = paymentService.syncPaymentStatus(transactionId);
        return Result.success(success);
    }

    /**
     * 获取支付统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getPaymentStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Map<String, Object> statistics = paymentService.getPaymentStatistics(startTime, endTime);
        return Result.success(statistics);
    }

    /**
     * 处理过期支付（定时任务调用）
     */
    @PostMapping("/handle-expired")
    public Result<Void> handleExpiredPayments() {
        paymentService.handleExpiredPayments();
        return Result.success();
    }
}