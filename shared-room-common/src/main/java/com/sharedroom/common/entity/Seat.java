package com.sharedroom.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 座位实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_seat")
public class Seat implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 座位ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 自习室ID
     */
    @TableField("study_room_id")
    private Long studyRoomId;

    /**
     * 座位编号
     */
    @TableField("seat_number")
    private String seatNumber;

    /**
     * 座位类型 1-普通座位 2-靠窗座位 3-VIP座位
     */
    @TableField("seat_type")
    private Integer seatType;

    /**
     * 座位状态 0-空闲 1-已预订 2-使用中 3-维护中
     */
    @TableField("status")
    private Integer status;

    /**
     * 价格(元/小时)
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 座位描述
     */
    @TableField("description")
    private String description;

    /**
     * 设施配置(JSON格式)
     */
    @TableField("facilities")
    private String facilities;

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