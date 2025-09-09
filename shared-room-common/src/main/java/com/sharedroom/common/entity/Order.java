package com.sharedroom.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 座位ID
     */
    @TableField("seat_id")
    private Long seatId;

    /**
     * 自习室ID
     */
    @TableField("study_room_id")
    private Long studyRoomId;

    /**
     * 预订开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 预订结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 预订时长(小时)
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 订单金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    @TableField("paid_amount")
    private BigDecimal paidAmount;

    /**
     * 实际金额
     */
    @TableField("actual_amount")
    private BigDecimal actualAmount;

    /**
     * 订单状态 0-待支付 1-已支付 2-使用中 3-已完成 4-已取消 5-已退款
     */
    @TableField("status")
    private Integer status;

    /**
     * 支付方式 1-支付宝 2-微信 3-余额
     */
    @TableField("pay_type")
    private Integer payType;

    /**
     * 支付时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /**
     * 支付流水号
     */
    @TableField("pay_no")
    private String payNo;

    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 取消时间
     */
    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

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
     * 逻辑删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}