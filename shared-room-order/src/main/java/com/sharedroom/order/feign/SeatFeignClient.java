package com.sharedroom.order.feign;

import com.sharedroom.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 座位服务Feign客户端
 */
@FeignClient(name = "shared-room-seat", path = "/api/seat")
public interface SeatFeignClient {

    /**
     * 检查座位是否可用
     */
    @GetMapping("/available/check/{seatId}")
    Result<Boolean> checkSeatAvailable(@PathVariable("seatId") Long seatId);

    /**
     * 预订座位
     */
    @PostMapping("/reserve/{seatId}")
    Result<Boolean> reserveSeat(@PathVariable("seatId") Long seatId);

    /**
     * 释放座位
     */
    @PostMapping("/release/{seatId}")
    Result<Boolean> releaseSeat(@PathVariable("seatId") Long seatId);

    /**
     * 获取座位详情
     */
    @GetMapping("/{seatId}")
    Result<Object> getSeatById(@PathVariable("seatId") Long seatId);
}