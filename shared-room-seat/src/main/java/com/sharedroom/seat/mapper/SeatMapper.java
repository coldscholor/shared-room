package com.sharedroom.seat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sharedroom.seat.entity.Seat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 座位Mapper接口
 */
@Mapper
public interface SeatMapper extends BaseMapper<Seat> {

    /**
     * 根据自习室ID和座位状态查询座位列表
     */
    @Select("SELECT * FROM seat WHERE study_room_id = #{studyRoomId} AND status = #{status} AND deleted = 0")
    List<Seat> selectByStudyRoomIdAndStatus(@Param("studyRoomId") Long studyRoomId, @Param("status") Integer status);

    /**
     * 根据地理位置搜索附近的座位
     * 使用MySQL的空间函数计算距离
     */
    @Select("SELECT s.* FROM seat s " +
            "INNER JOIN study_room sr ON s.study_room_id = sr.id " +
            "WHERE s.status = 1 AND s.deleted = 0 AND sr.deleted = 0 " +
            "AND ST_Distance_Sphere(POINT(sr.longitude, sr.latitude), POINT(#{longitude}, #{latitude})) <= #{radius} " +
            "ORDER BY ST_Distance_Sphere(POINT(sr.longitude, sr.latitude), POINT(#{longitude}, #{latitude}))")
    List<Seat> selectNearbySeats(@Param("longitude") BigDecimal longitude, 
                                @Param("latitude") BigDecimal latitude, 
                                @Param("radius") Integer radius);

    /**
     * 根据座位类型和价格范围查询座位
     */
    @Select("SELECT * FROM seat WHERE seat_type = #{seatType} " +
            "AND price BETWEEN #{minPrice} AND #{maxPrice} " +
            "AND status = 1 AND deleted = 0 " +
            "ORDER BY price ASC")
    List<Seat> selectBySeatTypeAndPriceRange(@Param("seatType") Integer seatType,
                                           @Param("minPrice") BigDecimal minPrice,
                                           @Param("maxPrice") BigDecimal maxPrice);

    /**
     * 更新座位状态
     */
    @Select("UPDATE seat SET status = #{status} WHERE id = #{seatId} AND deleted = 0")
    int updateSeatStatus(@Param("seatId") Long seatId, @Param("status") Integer status);
}