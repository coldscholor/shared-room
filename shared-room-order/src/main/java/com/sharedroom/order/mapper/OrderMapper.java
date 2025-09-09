package com.sharedroom.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sharedroom.common.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据用户ID查询订单列表
     */
    @Select("SELECT * FROM `order` WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<Order> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据订单状态查询订单列表
     */
    @Select("SELECT * FROM `order` WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<Order> selectByStatus(@Param("status") Integer status);

    /**
     * 根据座位ID查询订单列表
     */
    @Select("SELECT * FROM `order` WHERE seat_id = #{seatId} AND deleted = 0 ORDER BY create_time DESC")
    List<Order> selectBySeatId(@Param("seatId") Long seatId);

    /**
     * 查询超时未支付的订单
     */
    @Select("SELECT * FROM `order` WHERE status = 1 AND create_time < #{expireTime} AND deleted = 0")
    List<Order> selectExpiredOrders(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 更新订单状态
     */
    @Update("UPDATE `order` SET status = #{status}, update_time = NOW() WHERE id = #{orderId} AND deleted = 0")
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("status") Integer status);

    /**
     * 更新订单支付信息
     */
    @Update("UPDATE `order` SET status = #{status}, pay_method = #{payMethod}, pay_time = #{payTime}, " +
            "pay_transaction_id = #{payTransactionId}, update_time = NOW() " +
            "WHERE id = #{orderId} AND deleted = 0")
    int updatePaymentInfo(@Param("orderId") Long orderId,
                         @Param("status") Integer status,
                         @Param("payMethod") String payMethod,
                         @Param("payTime") LocalDateTime payTime,
                         @Param("payTransactionId") String payTransactionId);

    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo} AND deleted = 0")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询用户在指定时间段内的订单
     */
    @Select("SELECT * FROM `order` WHERE user_id = #{userId} " +
            "AND ((start_time BETWEEN #{startTime} AND #{endTime}) " +
            "OR (end_time BETWEEN #{startTime} AND #{endTime}) " +
            "OR (start_time <= #{startTime} AND end_time >= #{endTime})) " +
            "AND status IN (2, 3, 4) AND deleted = 0")
    List<Order> selectConflictOrders(@Param("userId") Long userId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);
}