package com.sharedroom.payment.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 支付请求DTO
 */
@Data
public class PaymentDTO {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 获取订单ID
     */
    public Long getOrderId() {
        return orderId;
    }

    /**
     * 设置订单ID
     */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0.01")
    private BigDecimal amount;

    /**
     * 获取支付金额
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * 设置支付金额
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * 支付方式 (alipay, wechat)
     */
    @NotBlank(message = "支付方式不能为空")
    private String payMethod;

    /**
     * 获取支付方式
     */
    public String getPayMethod() {
        return payMethod;
    }

    /**
     * 设置支付方式
     */
    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    /**
     * 支付描述
     */
    private String description;

    /**
     * 获取支付描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置支付描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 返回地址
     */
    private String returnUrl;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 扩展参数
     */
    private String extraParams;
}