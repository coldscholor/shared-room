package com.sharedroom.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 自习室实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_study_room")
public class StudyRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自习室ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 自习室名称
     */
    @TableField("name")
    private String name;

    /**
     * 地址
     */
    @TableField("address")
    private String address;

    /**
     * 经度
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 联系电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 营业开始时间
     */
    @TableField("open_time")
    private LocalTime openTime;

    /**
     * 营业结束时间
     */
    @TableField("close_time")
    private LocalTime closeTime;

    /**
     * 总座位数
     */
    @TableField("total_seats")
    private Integer totalSeats;

    /**
     * 可用座位数
     */
    @TableField("available_seats")
    private Integer availableSeats;

    /**
     * 自习室图片
     */
    @TableField("images")
    private String images;

    /**
     * 自习室描述
     */
    @TableField("description")
    private String description;

    /**
     * 设施配置(JSON格式)
     */
    @TableField("facilities")
    private String facilities;

    /**
     * 状态 0-关闭 1-营业
     */
    @TableField("status")
    private Integer status;

    /**
     * 评分
     */
    @TableField("rating")
    private BigDecimal rating;

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