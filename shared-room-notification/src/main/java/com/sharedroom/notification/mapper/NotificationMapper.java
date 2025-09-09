package com.sharedroom.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sharedroom.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知记录Mapper接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 根据用户ID查询通知记录（分页）
     */
    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    Page<Notification> selectByUserId(Page<Notification> page, @Param("userId") Long userId);

    /**
     * 根据用户ID和通知类型查询通知记录（分页）
     */
    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND type = #{type} AND deleted = 0 ORDER BY create_time DESC")
    Page<Notification> selectByUserIdAndType(Page<Notification> page, @Param("userId") Long userId, @Param("type") Integer type);

    /**
     * 根据用户ID和已读状态查询通知记录（分页）
     */
    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND is_read = #{isRead} AND deleted = 0 ORDER BY create_time DESC")
    Page<Notification> selectByUserIdAndReadStatus(Page<Notification> page, @Param("userId") Long userId, @Param("isRead") Boolean isRead);

    /**
     * 根据用户ID、通知类型和已读状态查询通知记录（分页）
     */
    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND type = #{type} AND is_read = #{isRead} AND deleted = 0 ORDER BY create_time DESC")
    Page<Notification> selectByUserIdAndTypeAndReadStatus(Page<Notification> page, @Param("userId") Long userId, @Param("type") Integer type, @Param("isRead") Boolean isRead);

    /**
     * 批量标记为已读
     */
    @Update("UPDATE notification SET is_read = 1, read_time = #{readTime} WHERE id IN (${ids}) AND user_id = #{userId}")
    int batchMarkAsRead(@Param("ids") String ids, @Param("userId") Long userId, @Param("readTime") LocalDateTime readTime);

    /**
     * 标记用户所有通知为已读
     */
    @Update("UPDATE notification SET is_read = 1, read_time = #{readTime} WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId, @Param("readTime") LocalDateTime readTime);

    /**
     * 获取用户未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0")
    Long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 获取用户未读通知数量（按类型分组）
     */
    @Select("SELECT type, COUNT(*) as count FROM notification WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0 GROUP BY type")
    List<Map<String, Object>> countUnreadByUserIdGroupByType(@Param("userId") Long userId);

    /**
     * 查询指定时间范围内的通知记录
     */
    @Select("SELECT * FROM notification WHERE create_time >= #{startTime} AND create_time <= #{endTime} AND deleted = 0 ORDER BY create_time DESC")
    List<Notification> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计通知发送情况
     */
    @Select("SELECT type, COUNT(*) as total_count, SUM(CASE WHEN is_read = 1 THEN 1 ELSE 0 END) as read_count " +
            "FROM notification WHERE create_time >= #{startTime} AND create_time <= #{endTime} AND deleted = 0 " +
            "GROUP BY type")
    List<Map<String, Object>> getNotificationStatistics(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 删除过期通知
     */
    @Update("UPDATE notification SET deleted = 1 WHERE create_time < #{expireTime}")
    int deleteExpiredNotifications(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 查询需要推送的通知（未推送且创建时间在指定范围内）
     */
    @Select("SELECT * FROM notification WHERE is_pushed = 0 AND create_time >= #{startTime} AND deleted = 0 ORDER BY create_time ASC LIMIT #{limit}")
    List<Notification> selectUnpushedNotifications(@Param("startTime") LocalDateTime startTime, @Param("limit") Integer limit);

    /**
     * 更新推送状态
     */
    @Update("UPDATE notification SET is_pushed = 1, push_time = #{pushTime} WHERE id = #{id}")
    int updatePushStatus(@Param("id") Long id, @Param("pushTime") LocalDateTime pushTime);

    /**
     * 批量更新推送状态
     */
    @Update("UPDATE notification SET is_pushed = 1, push_time = #{pushTime} WHERE id IN (${ids})")
    int batchUpdatePushStatus(@Param("ids") String ids, @Param("pushTime") LocalDateTime pushTime);
}