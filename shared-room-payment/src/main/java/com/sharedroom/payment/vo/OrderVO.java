package com.sharedroom.payment.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单VO（支付服务专用）
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
     * 获取订单状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置订单状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 订单状态名称
     */
    private String statusName;

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
}