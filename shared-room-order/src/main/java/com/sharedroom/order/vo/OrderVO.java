package com.sharedroom.order.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单VO
 */
@Data
public class OrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 座位ID
     */
    private Long seatId;

    /**
     * 座位编号
     */
    private String seatNumber;

    /**
     * 自习室ID
     */
    private Long studyRoomId;

    /**
     * 自习室名称
     */
    private String studyRoomName;

    /**
     * 自习室地址
     */
    private String studyRoomAddress;

    /**
     * 预订开始时间
     */
    private LocalDateTime startTime;

    /**
     * 预订结束时间
     */
    private LocalDateTime endTime;

    /**
     * 预订时长(小时)
     */
    private Integer duration;

    /**
     * 订单金额
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    private BigDecimal actualAmount;

    /**
     * 订单状态 1-待支付 2-已支付 3-使用中 4-已完成 5-已取消 6-已退款
     */
    private Integer status;

    /**
     * 订单状态名称
     */
    private String statusName;

    /**
     * 支付方式
     */
    private String payMethod;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 支付流水号
     */
    private String payTransactionId;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否可以取消
     */
    private Boolean canCancel;

    /**
     * 是否可以退款
     */
    private Boolean canRefund;

    /**
     * 剩余支付时间(分钟)
     */
    private Long remainingPayTime;
}