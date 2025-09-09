package com.sharedroom.seat.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 座位VO
 */
@Data
public class SeatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 座位ID
     */
    private Long id;

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
     * 座位类型 1-普通座位 2-靠窗座位 3-VIP座位
     */
    private Integer seatType;

    /**
     * 座位类型名称
     */
    private String seatTypeName;

    /**
     * 座位状态 0-维护中 1-可预订 2-已预订 3-使用中
     */
    private Integer status;

    /**
     * 座位状态名称
     */
    private String statusName;

    /**
     * 座位价格(元/小时)
     */
    private BigDecimal price;

    /**
     * 座位描述
     */
    private String description;

    /**
     * 座位设施配置
     */
    private String facilities;

    /**
     * 座位图片
     */
    private String images;

    /**
     * 座位位置(楼层-区域-座位号)
     */
    private String location;

    /**
     * 是否靠窗 0-否 1-是
     */
    private Integer isWindow;

    /**
     * 是否有电源 0-否 1-是
     */
    private Integer hasPower;

    /**
     * 是否有台灯 0-否 1-是
     */
    private Integer hasLamp;

    /**
     * 座位评分
     */
    private BigDecimal rating;

    /**
     * 评价数量
     */
    private Integer reviewCount;

    /**
     * 距离(米) - 用于地理位置搜索
     */
    private BigDecimal distance;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}