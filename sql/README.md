# Seata分布式事务配置说明

## 概述

本项目使用Seata实现分布式事务管理，采用AT模式（自动事务模式）来保证跨服务的数据一致性。

## 配置文件说明

### 1. shared-seata.yaml

这是Seata的共享配置文件，需要上传到Nacos配置中心：
- **命名空间**: `shared-room`
- **Group**: `SEATA_GROUP`
- **Data ID**: `shared-seata.yaml`

### 2. Bootstrap配置

各个微服务的`bootstrap.yml`已经配置了引用`shared-seata.yaml`：

```yaml
spring:
  cloud:
    nacos:
      config:
        shared-configs:
          - data-id: shared-seata.yaml
            group: SEATA_GROUP
            refresh: true
```

## 数据库表创建

### 执行步骤

1. **创建业务数据库**（如果还没有创建）：
   ```sql
   CREATE DATABASE shared_room;
   CREATE DATABASE shared_room_order;
   CREATE DATABASE shared_room_payment;
   CREATE DATABASE shared_room_notification;
   ```

2. **执行Seata表创建脚本**：
   ```bash
   mysql -u root -p < seata-tables.sql
   ```

### 表结构说明

#### 业务数据库表

每个业务数据库都需要创建`undo_log`表：
- **shared_room**: 用户服务和座位服务共享
- **shared_room_order**: 订单服务专用
- **shared_room_payment**: 支付服务专用
- **shared_room_notification**: 通知服务专用

#### Seata Server表（可选）

如果使用DB模式存储事务日志，需要创建以下表：
- `global_table`: 全局事务表
- `branch_table`: 分支事务表
- `lock_table`: 全局锁表
- `distributed_lock`: 分布式锁表

## Seata Server部署

### 1. 下载Seata Server

```bash
# 下载Seata 1.4.2
wget https://github.com/seata/seata/releases/download/v1.4.2/seata-server-1.4.2.tar.gz
tar -xzf seata-server-1.4.2.tar.gz
cd seata
```

### 2. 配置Seata Server

修改`conf/registry.conf`：

```conf
registry {
  type = "nacos"
  nacos {
    application = "seata-server"
    serverAddr = "localhost:8848"
    group = "SEATA_GROUP"
    namespace = "shared-room"
  }
}

config {
  type = "nacos"
  nacos {
    serverAddr = "localhost:8848"
    group = "SEATA_GROUP"
    namespace = "shared-room"
    dataId = "seataServer.properties"
  }
}
```

### 3. 上传Seata Server配置到Nacos

创建`seataServer.properties`配置文件并上传到Nacos：
- **命名空间**: `shared-room`
- **Group**: `SEATA_GROUP`
- **Data ID**: `seataServer.properties`

配置内容：
```properties
# 存储模式
store.mode=file
# 如果使用DB模式，配置数据库连接
# store.mode=db
# store.db.datasource=druid
# store.db.dbType=mysql
# store.db.driverClassName=com.mysql.cj.jdbc.Driver
# store.db.url=jdbc:mysql://localhost:3306/seata?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
# store.db.user=root
# store.db.password=123456

# 事务日志存储模式：file、db
store.file.dir=file_store/data
store.file.maxBranchSessionSize=16384
store.file.maxGlobalSessionSize=512
store.file.fileWriteBufferCacheSize=16384
store.file.flushDiskMode=async
store.file.sessionReloadReadSize=100

# 服务端配置
server.recovery.committingRetryPeriod=1000
server.recovery.asynCommittingRetryPeriod=1000
server.recovery.rollbackingRetryPeriod=1000
server.recovery.timeoutRetryPeriod=1000
server.maxCommitRetryTimeout=-1
server.maxRollbackRetryTimeout=-1
server.rollbackRetryTimeoutUnlockEnable=false
server.distributedLockExpireTime=10000

# 客户端配置
client.rm.asyncCommitBufferLimit=10000
client.rm.lock.retryInterval=10
client.rm.lock.retryTimes=30
client.rm.lock.retryPolicyBranchRollbackOnConflict=true
client.rm.reportRetryCount=5
client.rm.tableMetaCheckEnable=false
client.rm.tableMetaCheckerInterval=60000
client.rm.sqlParserType=druid
client.rm.reportSuccessEnable=false
client.rm.sagaBranchRegisterEnable=false
client.tm.commitRetryCount=5
client.tm.rollbackRetryCount=5
client.tm.defaultGlobalTransactionTimeout=60000
client.tm.degradeCheck=false
client.tm.degradeCheckAllowTimes=10
client.tm.degradeCheckPeriod=2000

# 传输配置
transport.serialization=seata
transport.compressor=none
transport.heartbeat=true
transport.enableClientBatchSendRequest=false
transport.threadFactory.bossThreadPrefix=NettyBoss
transport.threadFactory.workerThreadPrefix=NettyServerNIOWorker
transport.threadFactory.serverExecutorThreadPrefix=NettyServerBizHandler
transport.threadFactory.shareBossWorker=false
transport.threadFactory.clientSelectorThreadPrefix=NettyClientSelector
transport.threadFactory.clientSelectorThreadSize=1
transport.threadFactory.clientWorkerThreadPrefix=NettyClientWorkerThread
transport.threadFactory.bossThreadSize=1
transport.threadFactory.workerThreadSize=default
transport.shutdown.wait=3
```

### 4. 启动Seata Server

```bash
# Linux/Mac
sh seata-server.sh -p 8091 -h 127.0.0.1 -m file

# Windows
seata-server.bat -p 8091 -h 127.0.0.1 -m file
```

## 使用说明

### 1. 分布式事务注解

在需要分布式事务的方法上添加`@GlobalTransactional`注解：

```java
@Service
public class OrderServiceImpl implements OrderService {
    
    @GlobalTransactional(rollbackFor = Exception.class)
    public void createOrder(CreateOrderRequest request) {
        // 1. 创建订单
        orderMapper.insert(order);
        
        // 2. 调用座位服务锁定座位
        seatFeignClient.lockSeat(request.getSeatId());
        
        // 3. 调用支付服务创建支付记录
        paymentFeignClient.createPayment(paymentRequest);
    }
}
```

### 2. 事务传播

- `@GlobalTransactional`: 开启全局事务
- `@Transactional`: 本地事务，会自动加入全局事务

### 3. 异常处理

当分布式事务中任何一个服务抛出异常时，Seata会自动回滚所有已执行的操作。

## 监控和运维

### 1. 查看事务状态

可以通过Seata控制台或数据库表查看事务执行状态：

```sql
-- 查看全局事务
SELECT * FROM global_table WHERE status = 1; -- 1表示进行中

-- 查看分支事务
SELECT * FROM branch_table WHERE status = 1;

-- 查看回滚日志
SELECT * FROM undo_log;
```

### 2. 清理历史数据

Seata会自动清理已完成的事务数据，也可以手动清理：

```sql
-- 清理已完成的全局事务（状态为2表示已提交，3表示已回滚）
DELETE FROM global_table WHERE status IN (2, 3) AND gmt_modified < DATE_SUB(NOW(), INTERVAL 7 DAY);

-- 清理对应的分支事务
DELETE FROM branch_table WHERE xid NOT IN (SELECT xid FROM global_table);

-- 清理undo_log（各业务数据库）
DELETE FROM undo_log WHERE log_created < DATE_SUB(NOW(), INTERVAL 7 DAY);
```

## 故障排查

### 1. 常见问题

- **服务无法注册到Seata**: 检查Nacos配置和网络连接
- **事务回滚失败**: 检查undo_log表是否存在
- **分布式锁超时**: 调整`client.rm.lock.retryTimes`参数

### 2. 日志查看

```bash
# 查看Seata Server日志
tail -f logs/seata-server.log

# 查看应用日志中的Seata相关信息
grep -i seata application.log
```

### 3. 性能优化

- 合理设置事务超时时间
- 避免长事务
- 使用异步提交模式
- 定期清理历史数据