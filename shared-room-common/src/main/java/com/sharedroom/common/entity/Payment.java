package com.sharedroom.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_payment")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 支付流水号
     */
    @TableField("transaction_id")
    private String transactionId;

    /**
     * 第三方支付流水号
     */
    @TableField("third_party_transaction_id")
    private String thirdPartyTransactionId;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 支付金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 支付方式 (alipay, wechat)
     */
    @TableField("pay_method")
    private String payMethod;

    /**
     * 支付状态 (1:待支付 2:已支付 3:支付失败 4:已退款)
     */
    @TableField("status")
    private Integer status;

    /**
     * 支付描述
     */
    @TableField("description")
    private String description;

    /**
     * 支付时间
     */
    @TableField("paid_time")
    private LocalDateTime paidTime;

    /**
     * 二维码URL
     */
    @TableField("qr_code_url")
    private String qrCodeUrl;

    /**
     * 支付URL
     */
    @TableField("pay_url")
    private String payUrl;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}