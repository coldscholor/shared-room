package com.sharedroom.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sharedroom.common.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付记录Mapper接口
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    /**
     * 根据订单ID查询支付记录
     */
    @Select("SELECT * FROM payment WHERE order_id = #{orderId} AND deleted = 0")
    List<Payment> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据支付流水号查询支付记录
     */
    @Select("SELECT * FROM payment WHERE transaction_id = #{transactionId} AND deleted = 0")
    Payment selectByTransactionId(@Param("transactionId") String transactionId);

    /**
     * 根据第三方支付流水号查询支付记录
     */
    @Select("SELECT * FROM payment WHERE third_party_transaction_id = #{thirdPartyTransactionId} AND deleted = 0")
    Payment selectByThirdPartyTransactionId(@Param("thirdPartyTransactionId") String thirdPartyTransactionId);

    /**
     * 更新支付状态
     */
    @Update("UPDATE payment SET status = #{status}, update_time = #{updateTime} WHERE id = #{paymentId}")
    int updatePaymentStatus(@Param("paymentId") Long paymentId, 
                           @Param("status") Integer status, 
                           @Param("updateTime") LocalDateTime updateTime);

    /**
     * 更新支付成功信息
     */
    @Update("UPDATE payment SET status = 2, third_party_transaction_id = #{thirdPartyTransactionId}, " +
            "paid_time = #{paidTime}, update_time = #{updateTime} WHERE id = #{paymentId}")
    int updatePaymentSuccess(@Param("paymentId") Long paymentId,
                            @Param("thirdPartyTransactionId") String thirdPartyTransactionId,
                            @Param("paidTime") LocalDateTime paidTime,
                            @Param("updateTime") LocalDateTime updateTime);

    /**
     * 查询用户支付记录
     */
    @Select("SELECT * FROM payment WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<Payment> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询指定时间范围内的支付记录
     */
    @Select("SELECT * FROM payment WHERE create_time >= #{startTime} AND create_time <= #{endTime} " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<Payment> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计支付金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM payment WHERE status = 2 " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} AND deleted = 0")
    BigDecimal sumPaymentAmount(@Param("startTime") LocalDateTime startTime, 
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计支付笔数
     */
    @Select("SELECT COUNT(*) FROM payment WHERE status = 2 " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} AND deleted = 0")
    Long countPayments(@Param("startTime") LocalDateTime startTime, 
                      @Param("endTime") LocalDateTime endTime);

    /**
     * 查询超时未支付的记录
     */
    @Select("SELECT * FROM payment WHERE status = 1 AND create_time < #{expireTime} AND deleted = 0")
    List<Payment> selectExpiredPayments(@Param("expireTime") LocalDateTime expireTime);
}