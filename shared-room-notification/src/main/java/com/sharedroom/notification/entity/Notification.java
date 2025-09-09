package com.sharedroom.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知实体类
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 通知类型：1-系统通知，2-订单通知，3-支付通知，4-座位通知
     */
    private Integer type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 扩展数据
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Map<String, Object> extraData;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 是否已推送
     */
    private Boolean isPushed;

    /**
     * 是否推送
     */
    private Boolean isPush;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 推送时间
     */
    private LocalDateTime pushTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Boolean deleted;

    // 手动添加getter和setter方法
    public Long getId() {
        return id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public Integer getType() {
        return type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getContent() {
        return content;
    }
    
    public Long getBusinessId() {
        return businessId;
    }
    
    public String getBusinessType() {
        return businessType;
    }
    
    public Map<String, Object> getExtraData() {
        return extraData;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public Boolean getIsPushed() {
        return isPushed;
    }
    
    public Boolean getIsPush() {
        return isPush;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public LocalDateTime getReadTime() {
        return readTime;
    }
    
    public LocalDateTime getPushTime() {
        return pushTime;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setType(Integer type) {
        this.type = type;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }
    
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }
    
    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public void setIsPushed(Boolean isPushed) {
        this.isPushed = isPushed;
    }
    
    public void setIsPush(Boolean isPush) {
        this.isPush = isPush;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }
    
    public void setPushTime(LocalDateTime pushTime) {
        this.pushTime = pushTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}