package com.sharedroom.seat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sharedroom.common.result.Result;
import com.sharedroom.common.utils.UserContext;
import com.sharedroom.seat.dto.SeatSearchDTO;
import com.sharedroom.seat.entity.Seat;
import com.sharedroom.seat.service.SeatService;
import com.sharedroom.seat.vo.SeatVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 座位控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/seat")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    /**
     * 分页查询座位列表
     */
    @PostMapping("/page")
    public Result<Page<SeatVO>> getSeatPage(@RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestBody(required = false) SeatSearchDTO searchDTO) {
        Page<Seat> page = new Page<>(current, size);
        if (searchDTO == null) {
            searchDTO = new SeatSearchDTO();
        }
        Page<SeatVO> result = seatService.getSeatPage(page, searchDTO);
        return Result.success(result);
    }

    /**
     * 根据座位ID获取座位详情
     */
    @GetMapping("/{seatId}")
    public Result<SeatVO> getSeatById(@PathVariable Long seatId) {
        SeatVO seatVO = seatService.getSeatById(seatId);
        return Result.success(seatVO);
    }

    /**
     * 根据自习室ID查询可用座位
     */
    @GetMapping("/available/{studyRoomId}")
    public Result<List<SeatVO>> getAvailableSeats(@PathVariable Long studyRoomId) {
        List<SeatVO> seats = seatService.getAvailableSeatsByStudyRoomId(studyRoomId);
        return Result.success(seats);
    }

    /**
     * 地理位置搜索附近座位
     */
    @GetMapping("/nearby")
    public Result<List<SeatVO>> searchNearbySeats(@RequestParam BigDecimal longitude,
                                                 @RequestParam BigDecimal latitude,
                                                 @RequestParam(defaultValue = "5000") Integer radius) {
        List<SeatVO> seats = seatService.searchNearbySeats(longitude, latitude, radius);
        return Result.success(seats);
    }

    /**
     * 根据座位类型和价格范围搜索座位
     */
    @GetMapping("/search")
    public Result<List<SeatVO>> searchSeats(@RequestParam(required = false) Integer seatType,
                                           @RequestParam(required = false) BigDecimal minPrice,
                                           @RequestParam(required = false) BigDecimal maxPrice) {
        List<SeatVO> seats = seatService.searchSeatsByTypeAndPrice(seatType, minPrice, maxPrice);
        return Result.success(seats);
    }

    /**
     * 预订座位
     */
    @PostMapping("/reserve/{seatId}")
    public Result<Boolean> reserveSeat(@PathVariable Long seatId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        boolean success = seatService.reserveSeat(seatId, userId);
        if (success) {
            return Result.success("座位预订成功", true);
        } else {
            return Result.error("座位预订失败");
        }
    }

    /**
     * 释放座位
     */
    @PostMapping("/release/{seatId}")
    public Result<Boolean> releaseSeat(@PathVariable Long seatId) {
        boolean success = seatService.releaseSeat(seatId);
        if (success) {
            return Result.success("座位释放成功", true);
        } else {
            return Result.error("座位释放失败");
        }
    }

    /**
     * 检查座位是否可用
     */
    @GetMapping("/available/check/{seatId}")
    public Result<Boolean> checkSeatAvailable(@PathVariable Long seatId) {
        boolean available = seatService.isSeatAvailable(seatId);
        return Result.success(available);
    }

    /**
     * 获取热门座位推荐
     */
    @GetMapping("/popular")
    public Result<List<SeatVO>> getPopularSeats(@RequestParam(defaultValue = "10") Integer limit) {
        List<SeatVO> seats = seatService.getPopularSeats(limit);
        return Result.success(seats);
    }

    /**
     * 更新座位状态(管理员接口)
     */
    @PutMapping("/status/{seatId}")
    public Result<Boolean> updateSeatStatus(@PathVariable Long seatId,
                                           @RequestParam Integer status) {
        boolean success = seatService.updateSeatStatus(seatId, status);
        if (success) {
            return Result.success("座位状态更新成功", true);
        } else {
            return Result.error("座位状态更新失败");
        }
    }
}