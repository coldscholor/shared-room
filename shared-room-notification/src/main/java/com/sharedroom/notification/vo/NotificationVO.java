package com.sharedroom.notification.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知返回VO
 */
@Data
public class NotificationVO {

    private Long id;

    private Long userId;

    private String username;

    private Integer type;

    private String typeName;

    private String title;

    private String content;

    private Long businessId;

    private String businessType;

    private Map<String, Object> extraData;

    private Boolean isRead;

    private Boolean isPushed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pushTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 手动添加getter和setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Integer getType() {
        return type;
    }
    
    public void setType(Integer type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Long getBusinessId() {
        return businessId;
    }
    
    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }
    
    public String getBusinessType() {
        return businessType;
    }
    
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }
    
    public Map<String, Object> getExtraData() {
        return extraData;
    }
    
    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public Boolean getIsPushed() {
        return isPushed;
    }
    
    public void setIsPushed(Boolean isPushed) {
        this.isPushed = isPushed;
    }
    
    public LocalDateTime getReadTime() {
        return readTime;
    }
    
    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }
    
    public LocalDateTime getPushTime() {
        return pushTime;
    }
    
    public void setPushTime(LocalDateTime pushTime) {
        this.pushTime = pushTime;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取类型名称
     */
    public String getTypeName() {
        if (type == null) {
            return "未知";
        }
        switch (type) {
            case 1:
                return "系统通知";
            case 2:
                return "订单通知";
            case 3:
                return "支付通知";
            case 4:
                return "座位通知";
            default:
                return "其他";
        }
    }
}