package com.sharedroom.seat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sharedroom.seat.dto.SeatSearchDTO;
import com.sharedroom.seat.entity.Seat;
import com.sharedroom.seat.vo.SeatVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 座位服务接口
 */
public interface SeatService extends IService<Seat> {

    /**
     * 分页查询座位列表
     */
    Page<SeatVO> getSeatPage(Page<Seat> page, SeatSearchDTO searchDTO);

    /**
     * 根据自习室ID查询可用座位
     */
    List<SeatVO> getAvailableSeatsByStudyRoomId(Long studyRoomId);

    /**
     * 根据地理位置搜索附近的座位
     */
    List<SeatVO> searchNearbySeats(BigDecimal longitude, BigDecimal latitude, Integer radius);

    /**
     * 根据座位ID获取座位详情
     */
    SeatVO getSeatById(Long seatId);

    /**
     * 预订座位(使用分布式锁防止超卖)
     */
    boolean reserveSeat(Long seatId, Long userId);

    /**
     * 释放座位
     */
    boolean releaseSeat(Long seatId);

    /**
     * 检查座位是否可用
     */
    boolean isSeatAvailable(Long seatId);

    /**
     * 更新座位状态
     */
    boolean updateSeatStatus(Long seatId, Integer status);

    /**
     * 根据座位类型和价格范围搜索座位
     */
    List<SeatVO> searchSeatsByTypeAndPrice(Integer seatType, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 获取热门座位推荐
     */
    List<SeatVO> getPopularSeats(Integer limit);
}