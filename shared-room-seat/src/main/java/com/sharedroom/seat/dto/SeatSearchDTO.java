package com.sharedroom.seat.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 座位搜索DTO
 */
@Data
public class SeatSearchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自习室ID
     */
    private Long studyRoomId;

    /**
     * 座位类型 1-普通座位 2-靠窗座位 3-VIP座位
     */
    private Integer seatType;

    /**
     * 座位状态 0-维护中 1-可预订 2-已预订 3-使用中
     */
    private Integer status;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

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
     * 经度(用于地理位置搜索)
     */
    private BigDecimal longitude;

    /**
     * 纬度(用于地理位置搜索)
     */
    private BigDecimal latitude;

    /**
     * 搜索半径(米)
     */
    private Integer radius;

    /**
     * 排序字段 price-价格 rating-评分 distance-距离
     */
    private String sortBy;

    /**
     * 排序方向 asc-升序 desc-降序
     */
    private String sortOrder;

    /**
     * 关键词搜索
     */
    private String keyword;
}