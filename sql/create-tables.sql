-- ================================================================================================
-- 共享自习室项目 - 完整数据库表创建脚本
-- ================================================================================================

-- ================================================================================================
-- 1. 创建数据库
-- ================================================================================================

-- 创建主数据库（用户服务和座位服务）
CREATE DATABASE IF NOT EXISTS `shared_room` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建订单服务数据库
CREATE DATABASE IF NOT EXISTS `shared_room_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建支付服务数据库
CREATE DATABASE IF NOT EXISTS `shared_room_payment` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建通知服务数据库
CREATE DATABASE IF NOT EXISTS `shared_room_notification` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ================================================================================================
-- 2. shared_room 数据库表（用户服务和座位服务）
-- ================================================================================================

USE shared_room;

-- 用户表
CREATE TABLE IF NOT EXISTS `tb_user` (
  `id` bigint(20) NOT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `gender` tinyint(1) DEFAULT NULL COMMENT '性别 0-女 1-男',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-正常',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间',
  `update_time` datetime(6) NOT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 自习室表
CREATE TABLE IF NOT EXISTS `tb_study_room` (
  `id` bigint(20) NOT NULL COMMENT '自习室ID',
  `name` varchar(100) NOT NULL COMMENT '自习室名称',
  `address` varchar(255) NOT NULL COMMENT '地址',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `open_time` time DEFAULT NULL COMMENT '营业开始时间',
  `close_time` time DEFAULT NULL COMMENT '营业结束时间',
  `total_seats` int(11) NOT NULL DEFAULT '0' COMMENT '总座位数',
  `available_seats` int(11) NOT NULL DEFAULT '0' COMMENT '可用座位数',
  `images` text COMMENT '自习室图片',
  `description` text COMMENT '自习室描述',
  `facilities` text COMMENT '设施配置(JSON格式)',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 0-关闭 1-营业',
  `rating` decimal(3,2) DEFAULT '0.00' COMMENT '评分',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间',
  `update_time` datetime(6) NOT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_location` (`longitude`,`latitude`),
  KEY `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自习室表';

-- 座位表
CREATE TABLE IF NOT EXISTS `tb_seat` (
  `id` bigint(20) NOT NULL COMMENT '座位ID',
  `study_room_id` bigint(20) NOT NULL COMMENT '自习室ID',
  `seat_number` varchar(20) NOT NULL COMMENT '座位编号',
  `seat_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '座位类型 1-普通座位 2-靠窗座位 3-VIP座位',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '座位状态 0-维护中 1-可预订 2-已预订 3-使用中',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '价格(元/小时)',
  `description` text COMMENT '座位描述',
  `facilities` text COMMENT '设施配置(JSON格式)',
  `images` text COMMENT '座位图片',
  `location` varchar(100) DEFAULT NULL COMMENT '座位位置(楼层-区域-座位号)',
  `is_window` tinyint(1) DEFAULT '0' COMMENT '是否靠窗 0-否 1-是',
  `has_power` tinyint(1) DEFAULT '0' COMMENT '是否有电源 0-否 1-是',
  `has_lamp` tinyint(1) DEFAULT '0' COMMENT '是否有台灯 0-否 1-是',
  `rating` decimal(3,2) DEFAULT '0.00' COMMENT '座位评分',
  `review_count` int(11) DEFAULT '0' COMMENT '评价数量',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间',
  `update_time` datetime(6) NOT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_study_room_id` (`study_room_id`),
  KEY `idx_status` (`status`),
  KEY `idx_seat_type` (`seat_type`),
  KEY `idx_price` (`price`),
  CONSTRAINT `fk_seat_study_room` FOREIGN KEY (`study_room_id`) REFERENCES `tb_study_room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

-- shared_room数据库的undo_log表
CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id` bigint(20) NOT NULL COMMENT 'branch transaction id',
  `xid` varchar(128) NOT NULL COMMENT 'global transaction id',
  `context` varchar(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` longblob NOT NULL COMMENT 'rollback info',
  `log_status` int(11) NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` datetime(6) NOT NULL COMMENT 'create datetime',
  `log_modified` datetime(6) NOT NULL COMMENT 'modify datetime',
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';

-- ================================================================================================
-- 3. shared_room_order 数据库表（订单服务）
-- ================================================================================================

USE shared_room_order;

-- 订单表
CREATE TABLE IF NOT EXISTS `tb_order` (
  `id` bigint(20) NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `seat_id` bigint(20) NOT NULL COMMENT '座位ID',
  `study_room_id` bigint(20) NOT NULL COMMENT '自习室ID',
  `start_time` datetime(6) NOT NULL COMMENT '预订开始时间',
  `end_time` datetime(6) NOT NULL COMMENT '预订结束时间',
  `duration` int(11) NOT NULL COMMENT '预订时长(小时)',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总金额',
  `paid_amount` decimal(10,2) DEFAULT '0.00' COMMENT '实付金额',
  `actual_amount` decimal(10,2) DEFAULT '0.00' COMMENT '实际金额',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '订单状态 0-待支付 1-已支付 2-使用中 3-已完成 4-已取消 5-已退款',
  `pay_type` tinyint(1) DEFAULT NULL COMMENT '支付方式 1-支付宝 2-微信 3-余额',
  `pay_time` datetime(6) DEFAULT NULL COMMENT '支付时间',
  `pay_no` varchar(64) DEFAULT NULL COMMENT '支付流水号',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `cancel_time` datetime(6) DEFAULT NULL COMMENT '取消时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间',
  `update_time` datetime(6) NOT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_seat_id` (`seat_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- shared_room_order数据库的undo_log表
CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id` bigint(20) NOT NULL COMMENT 'branch transaction id',
  `xid` varchar(128) NOT NULL COMMENT 'global transaction id',
  `context` varchar(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` longblob NOT NULL COMMENT 'rollback info',
  `log_status` int(11) NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` datetime(6) NOT NULL COMMENT 'create datetime',
  `log_modified` datetime(6) NOT NULL COMMENT 'modify datetime',
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';

-- ================================================================================================
-- 4. shared_room_payment 数据库表（支付服务）
-- ================================================================================================

USE shared_room_payment;

-- 支付记录表
CREATE TABLE IF NOT EXISTS `tb_payment` (
  `id` bigint(20) NOT NULL COMMENT '支付ID',
  `transaction_id` varchar(64) NOT NULL COMMENT '支付流水号',
  `third_party_transaction_id` varchar(64) DEFAULT NULL COMMENT '第三方支付流水号',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '支付金额',
  `pay_method` varchar(20) NOT NULL COMMENT '支付方式 (alipay, wechat)',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '支付状态 (1:待支付 2:已支付 3:支付失败 4:已退款)',
  `description` varchar(255) DEFAULT NULL COMMENT '支付描述',
  `paid_time` datetime(6) DEFAULT NULL COMMENT '支付时间',
  `qr_code_url` varchar(255) DEFAULT NULL COMMENT '二维码URL',
  `pay_url` varchar(255) DEFAULT NULL COMMENT '支付URL',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间',
  `update_time` datetime(6) NOT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- shared_room_payment数据库的undo_log表
CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id` bigint(20) NOT NULL COMMENT 'branch transaction id',
  `xid` varchar(128) NOT NULL COMMENT 'global transaction id',
  `context` varchar(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` longblob NOT NULL COMMENT 'rollback info',
  `log_status` int(11) NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` datetime(6) NOT NULL COMMENT 'create datetime',
  `log_modified` datetime(6) NOT NULL COMMENT 'modify datetime',
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';

-- ================================================================================================
-- 5. shared_room_notification 数据库表（通知服务）
-- ================================================================================================

USE shared_room_notification;

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `type` tinyint(1) NOT NULL COMMENT '通知类型：1-系统通知，2-订单通知，3-支付通知，4-座位通知',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `business_id` bigint(20) DEFAULT NULL COMMENT '业务ID',
  `business_type` varchar(20) DEFAULT NULL COMMENT '业务类型',
  `extra_data` json DEFAULT NULL COMMENT '扩展数据',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读 0-未读 1-已读',
  `is_pushed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已推送 0-未推送 1-已推送',
  `is_push` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否推送 0-不推送 1-推送',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱地址',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号码',
  `read_time` datetime(6) DEFAULT NULL COMMENT '阅读时间',
  `push_time` datetime(6) DEFAULT NULL COMMENT '推送时间',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间',
  `update_time` datetime(6) NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- shared_room_notification数据库的undo_log表
CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id` bigint(20) NOT NULL COMMENT 'branch transaction id',
  `xid` varchar(128) NOT NULL COMMENT 'global transaction id',
  `context` varchar(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` longblob NOT NULL COMMENT 'rollback info',
  `log_status` int(11) NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` datetime(6) NOT NULL COMMENT 'create datetime',
  `log_modified` datetime(6) NOT NULL COMMENT 'modify datetime',
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';

-- ================================================================================================
-- 执行说明
-- ================================================================================================
-- 1. 首先确保MySQL服务正在运行
-- 2. 使用root用户或具有创建数据库权限的用户执行此脚本
-- 3. 执行命令：mysql -u root -p < create-tables.sql
-- 4. 或者在MySQL客户端中执行：source /path/to/create-tables.sql
-- 5. 验证表创建成功：
--    - 检查各数据库是否创建成功
--    - 检查各业务表是否创建成功
--    - 检查各数据库中的undo_log表是否创建成功
-- ================================================================================================