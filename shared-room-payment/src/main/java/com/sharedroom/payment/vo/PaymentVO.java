package com.sharedroom.payment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录VO
 */
@Data
public class PaymentVO {

    /**
     * 支付ID
     */
    private Long id;

    /**
     * 支付流水号
     */
    private String transactionId;

    /**
     * 第三方支付流水号
     */
    private String thirdPartyTransactionId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式
     */
    private String payMethod;

    /**
     * 支付方式名称
     */
    private String payMethodName;

    /**
     * 设置支付方式名称
     */
    public void setPayMethodName(String payMethodName) {
        this.payMethodName = payMethodName;
    }

    /**
     * 获取支付方式名称
     */
    public String getPayMethodName() {
        return payMethodName;
    }

    /**
     * 支付状态 (1:待支付 2:已支付 3:支付失败 4:已退款)
     */
    private Integer status;

    /**
     * 支付状态名称
     */
    private String statusName;

    /**
     * 设置支付状态名称
     */
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    /**
     * 获取支付状态名称
     */
    public String getStatusName() {
        return statusName;
    }

    /**
     * 支付描述
     */
    private String description;

    /**
     * 支付时间
     */
    private LocalDateTime paidTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 支付二维码URL（用于扫码支付）
     */
    private String qrCodeUrl;

    /**
     * 设置支付二维码URL
     */
    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    /**
     * 获取支付二维码URL
     */
    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    /**
     * 支付页面URL（用于跳转支付）
     */
    private String payUrl;

    /**
     * 设置支付页面URL
     */
    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    /**
     * 获取支付页面URL
     */
    public String getPayUrl() {
        return payUrl;
    }

    /**
     * 是否可以取消
     */
    private Boolean canCancel;

    /**
     * 设置是否可以取消
     */
    public void setCanCancel(Boolean canCancel) {
        this.canCancel = canCancel;
    }

    /**
     * 获取是否可以取消
     */
    public Boolean getCanCancel() {
        return canCancel;
    }

    /**
     * 是否可以退款
     */
    private Boolean canRefund;

    /**
     * 设置是否可以退款
     */
    public void setCanRefund(Boolean canRefund) {
        this.canRefund = canRefund;
    }

    /**
     * 获取是否可以退款
     */
    public Boolean getCanRefund() {
        return canRefund;
    }

    /**
     * 剩余支付时间(分钟)
     */
    private Long remainingPayTime;

    /**
     * 设置剩余支付时间
     */
    public void setRemainingPayTime(Long remainingPayTime) {
        this.remainingPayTime = remainingPayTime;
    }

    /**
     * 获取剩余支付时间
     */
    public Long getRemainingPayTime() {
        return remainingPayTime;
    }
}