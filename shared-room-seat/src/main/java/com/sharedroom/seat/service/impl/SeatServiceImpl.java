package com.sharedroom.seat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharedroom.common.exception.BusinessException;
import com.sharedroom.common.result.ResultCode;
import com.sharedroom.seat.dto.SeatSearchDTO;
import com.sharedroom.seat.entity.Seat;
import com.sharedroom.seat.mapper.SeatMapper;
import com.sharedroom.seat.service.SeatService;
import com.sharedroom.seat.vo.SeatVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 座位服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatServiceImpl extends ServiceImpl<SeatMapper, Seat> implements SeatService {

    private final SeatMapper seatMapper;
    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SEAT_LOCK_PREFIX = "seat:lock:";
    private static final String SEAT_STATUS_PREFIX = "seat:status:";
    private static final int LOCK_WAIT_TIME = 3;
    private static final int LOCK_LEASE_TIME = 10;

    @Override
    public Page<SeatVO> getSeatPage(Page<Seat> page, SeatSearchDTO searchDTO) {
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        wrapper.eq(searchDTO.getStudyRoomId() != null, Seat::getStudyRoomId, searchDTO.getStudyRoomId())
                .eq(searchDTO.getSeatType() != null, Seat::getSeatType, searchDTO.getSeatType())
                .eq(searchDTO.getStatus() != null, Seat::getStatus, searchDTO.getStatus())
                .eq(searchDTO.getIsWindow() != null, Seat::getIsWindow, searchDTO.getIsWindow())
                .eq(searchDTO.getHasPower() != null, Seat::getHasPower, searchDTO.getHasPower())
                .eq(searchDTO.getHasLamp() != null, Seat::getHasLamp, searchDTO.getHasLamp())
                .ge(searchDTO.getMinPrice() != null, Seat::getPrice, searchDTO.getMinPrice())
                .le(searchDTO.getMaxPrice() != null, Seat::getPrice, searchDTO.getMaxPrice())
                .like(StringUtils.hasText(searchDTO.getKeyword()), Seat::getDescription, searchDTO.getKeyword())
                .or()
                .like(StringUtils.hasText(searchDTO.getKeyword()), Seat::getLocation, searchDTO.getKeyword());

        // 排序
        if (StringUtils.hasText(searchDTO.getSortBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(searchDTO.getSortOrder());
            switch (searchDTO.getSortBy()) {
                case "price":
                    wrapper.orderBy(true, isAsc, Seat::getPrice);
                    break;
                case "rating":
                    wrapper.orderBy(true, isAsc, Seat::getRating);
                    break;
                default:
                    wrapper.orderByDesc(Seat::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Seat::getCreateTime);
        }

        Page<Seat> seatPage = this.page(page, wrapper);
        
        // 转换为VO
        Page<SeatVO> voPage = new Page<>();
        BeanUtils.copyProperties(seatPage, voPage);
        List<SeatVO> voList = seatPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public List<SeatVO> getAvailableSeatsByStudyRoomId(Long studyRoomId) {
        List<Seat> seats = seatMapper.selectByStudyRoomIdAndStatus(studyRoomId, 1);
        return seats.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<SeatVO> searchNearbySeats(BigDecimal longitude, BigDecimal latitude, Integer radius) {
        if (radius == null || radius <= 0) {
            radius = 5000; // 默认5公里
        }
        List<Seat> seats = seatMapper.selectNearbySeats(longitude, latitude, radius);
        return seats.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public SeatVO getSeatById(Long seatId) {
        Seat seat = this.getById(seatId);
        if (seat == null) {
            throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        }
        return convertToVO(seat);
    }

    @Override
    public boolean reserveSeat(Long seatId, Long userId) {
        String lockKey = SEAT_LOCK_PREFIX + seatId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 尝试获取分布式锁
            if (lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS)) {
                try {
                    // 检查座位是否可用
                    if (!isSeatAvailable(seatId)) {
                        throw new BusinessException(ResultCode.SEAT_NOT_AVAILABLE);
                    }
                    
                    // 更新座位状态为已预订
                    boolean updated = updateSeatStatus(seatId, 2);
                    if (updated) {
                        // 缓存座位状态
                        String statusKey = SEAT_STATUS_PREFIX + seatId;
                        redisTemplate.opsForValue().set(statusKey, 2, 30, TimeUnit.MINUTES);
                        log.info("座位预订成功: seatId={}, userId={}", seatId, userId);
                        return true;
                    }
                    return false;
                } finally {
                    lock.unlock();
                }
            } else {
                throw new BusinessException(ResultCode.SEAT_LOCK_FAILED);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ResultCode.SEAT_LOCK_FAILED);
        }
    }

    @Override
    public boolean releaseSeat(Long seatId) {
        String lockKey = SEAT_LOCK_PREFIX + seatId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            if (lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS)) {
                try {
                    // 更新座位状态为可预订
                    boolean updated = updateSeatStatus(seatId, 1);
                    if (updated) {
                        // 清除缓存
                        String statusKey = SEAT_STATUS_PREFIX + seatId;
                        redisTemplate.delete(statusKey);
                        log.info("座位释放成功: seatId={}", seatId);
                        return true;
                    }
                    return false;
                } finally {
                    lock.unlock();
                }
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean isSeatAvailable(Long seatId) {
        // 先从缓存查询
        String statusKey = SEAT_STATUS_PREFIX + seatId;
        Object cachedStatus = redisTemplate.opsForValue().get(statusKey);
        if (cachedStatus != null) {
            return Integer.valueOf(1).equals(cachedStatus);
        }
        
        // 缓存未命中，查询数据库
        Seat seat = this.getById(seatId);
        return seat != null && seat.getStatus() == 1;
    }

    @Override
    public boolean updateSeatStatus(Long seatId, Integer status) {
        return seatMapper.updateSeatStatus(seatId, status) > 0;
    }

    @Override
    public List<SeatVO> searchSeatsByTypeAndPrice(Integer seatType, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Seat> seats = seatMapper.selectBySeatTypeAndPriceRange(seatType, minPrice, maxPrice);
        return seats.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<SeatVO> getPopularSeats(Integer limit) {
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Seat::getStatus, 1)
                .orderByDesc(Seat::getRating)
                .orderByDesc(Seat::getReviewCount)
                .last("LIMIT " + (limit != null ? limit : 10));
        
        List<Seat> seats = this.list(wrapper);
        return seats.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 转换为VO对象
     */
    private SeatVO convertToVO(Seat seat) {
        SeatVO vo = new SeatVO();
        BeanUtils.copyProperties(seat, vo);
        
        // 设置座位类型名称
        vo.setSeatTypeName(getSeatTypeName(seat.getSeatType()));
        
        // 设置座位状态名称
        vo.setStatusName(getSeatStatusName(seat.getStatus()));
        
        return vo;
    }

    /**
     * 获取座位类型名称
     */
    private String getSeatTypeName(Integer seatType) {
        if (seatType == null) return "未知";
        switch (seatType) {
            case 1: return "普通座位";
            case 2: return "靠窗座位";
            case 3: return "VIP座位";
            default: return "未知";
        }
    }

    /**
     * 获取座位状态名称
     */
    private String getSeatStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "维护中";
            case 1: return "可预订";
            case 2: return "已预订";
            case 3: return "使用中";
            default: return "未知";
        }
    }
}