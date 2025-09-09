package com.sharedroom.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 通用响应码
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 用户相关
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_NOT_LOGIN(1002, "用户未登录"),
    USER_ALREADY_EXIST(1003, "用户已存在"),
    USERNAME_OR_PASSWORD_ERROR(1004, "用户名或密码错误"),
    USER_DISABLED(1005, "用户已被禁用"),
    TOKEN_INVALID(1006, "Token无效"),
    TOKEN_EXPIRED(1007, "Token已过期"),
    LOGIN_REQUIRED(1008, "请先登录"),
    PHONE_ALREADY_EXIST(1009, "手机号已存在"),
    EMAIL_ALREADY_EXIST(1010, "邮箱已存在"),

    // 座位相关
    SEAT_NOT_EXIST(2001, "座位不存在"),
    SEAT_NOT_FOUND(2002, "座位未找到"),
    SEAT_NOT_AVAILABLE(2003, "座位不可用"),
    SEAT_ALREADY_BOOKED(2004, "座位已被预订"),
    SEAT_BOOKING_CONFLICT(2005, "座位预订时间冲突"),
    SEAT_BOOKING_LIMIT(2006, "超出座位预订限制"),
    SEAT_LOCK_FAILED(2007, "座位锁定失败"),
    SEAT_RESERVE_FAILED(2008, "座位预约失败"),

    // 订单相关
    ORDER_NOT_EXIST(3001, "订单不存在"),
    ORDER_NOT_FOUND(3002, "订单未找到"),
    ORDER_STATUS_ERROR(3003, "订单状态错误"),
    ORDER_START_TIME_ERROR(3004, "订单开始时间错误"),
    ORDER_CANNOT_CANCEL(3005, "订单无法取消"),
    ORDER_ALREADY_PAID(3006, "订单已支付"),
    ORDER_PAYMENT_TIMEOUT(3007, "订单支付超时"),
    ORDER_END_TIME_ERROR(3008, "订单结束时间错误"),
    ORDER_DURATION_ERROR(3009, "订单时长错误"),
    ORDER_EXPIRED(3010, "订单已过期"),
    ORDER_NOT_BELONG_TO_USER(3011, "订单不属于当前用户"),
    ORDER_TIME_CONFLICT(3012, "订单时间冲突"),

    // 支付相关
    PAYMENT_ERROR(4001, "支付失败"),
    PAYMENT_AMOUNT_ERROR(4002, "支付金额错误"),
    PAYMENT_CALLBACK_ERROR(4003, "支付回调处理失败"),
    REFUND_ERROR(4004, "退款失败"),
    PAYMENT_CREATE_FAILED(4005, "创建支付失败"),
    PAYMENT_NOT_FOUND(4006, "支付记录不存在"),
    PAYMENT_STATUS_ERROR(4007, "支付状态错误"),
    PAYMENT_METHOD_NOT_SUPPORTED(4008, "不支持的支付方式"),

    // 自习室相关
    STUDY_ROOM_NOT_EXIST(5001, "自习室不存在"),
    STUDY_ROOM_CLOSED(5002, "自习室已关闭"),
    STUDY_ROOM_FULL(5003, "自习室已满"),

    // 系统相关
    SYSTEM_BUSY(9001, "系统繁忙，请稍后重试"),
    DISTRIBUTED_LOCK_ERROR(9002, "获取分布式锁失败"),
    MESSAGE_SEND_ERROR(9003, "消息发送失败"),
    FILE_UPLOAD_ERROR(9004, "文件上传失败"),
    DATA_SYNC_ERROR(9005, "数据同步失败");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;
}