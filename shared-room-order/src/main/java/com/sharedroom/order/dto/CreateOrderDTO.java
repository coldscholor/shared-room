package com.sharedroom.order.dto;

import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 创建订单DTO
 */
@Data
public class CreateOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 座位ID
     */
    @NotNull(message = "座位ID不能为空")
    private Long seatId;

    /**
     * 预订开始时间
     */
    @NotNull(message = "预订开始时间不能为空")
    @Future(message = "预订开始时间必须是未来时间")
    private LocalDateTime startTime;

    /**
     * 预订结束时间
     */
    @NotNull(message = "预订结束时间不能为空")
    @Future(message = "预订结束时间必须是未来时间")
    private LocalDateTime endTime;

    /**
     * 预订时长(小时)
     */
    @Positive(message = "预订时长必须大于0")
    private Integer duration;

    /**
     * 备注
     */
    private String remark;
}