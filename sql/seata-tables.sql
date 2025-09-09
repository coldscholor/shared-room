-- ================================================================================================
-- Seata数据库表创建脚本
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
-- 2. 业务数据库表 - 每个业务数据库都需要创建undo_log表
-- ================================================================================================

-- 在shared_room数据库中创建undo_log表（用户服务和座位服务）
USE shared_room;
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

-- 在shared_room_order数据库中创建undo_log表（订单服务）
USE shared_room_order;
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

-- 在shared_room_payment数据库中创建undo_log表（支付服务）
USE shared_room_payment;
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

-- 在shared_room_notification数据库中创建undo_log表（通知服务）
USE shared_room_notification;
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
-- 执行说明：
-- 1. 首先确保MySQL服务正在运行
-- 2. 使用root用户或具有创建数据库权限的用户执行此脚本
-- 3. 执行命令：mysql -u root -p < seata-tables.sql
-- 4. 或者在MySQL客户端中执行：source /path/to/seata-tables.sql
-- 5. 验证表创建成功：检查各业务数据库中的undo_log表
-- 6. 注意：本脚本只创建业务数据库的undo_log表，Seata Server使用file模式存储
-- ================================================================================================