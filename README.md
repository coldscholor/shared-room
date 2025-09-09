# 共享自习室管理系统

## 项目简介

共享自习室管理系统是一个基于Spring Cloud微服务架构的现代化自习室预订平台。系统提供座位预订、在线支付、实时通知等功能，为用户提供便捷的自习室使用体验。

## 技术栈

### 后端技术
- **Spring Boot 2.7.0** - 微服务框架
- **Spring Cloud 2021.0.3** - 微服务治理
- **Spring Cloud Gateway** - API网关
- **Nacos 2.1.0** - 服务注册与配置中心
- **MyBatis Plus 3.5.2** - ORM框架
- **MySQL 8.0** - 关系型数据库
- **Redis 7.0** - 缓存数据库
- **RocketMQ 4.9.4** - 消息队列
- **Seata 1.6.1** - 分布式事务
- **JWT** - 身份认证
- **WebSocket** - 实时通信
- **支付宝SDK** - 支付集成

### 开发工具
- **Maven 3.6+** - 项目构建
- **JDK 1.8+** - 开发环境
- **Git** - 版本控制

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web前端       │    │   移动端APP     │    │   管理后台      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  API Gateway    │
                    │   (端口:8080)   │
                    └─────────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                       │                       │
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ 用户服务    │    │ 座位服务    │    │ 订单服务    │    │ 支付服务    │
│ (端口:8081) │    │ (端口:8082) │    │ (端口:8083) │    │ (端口:8084) │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                       │                       │                │
        └───────────────────────┼───────────────────────┼────────────────┘
                                │                       │
                    ┌─────────────────┐    ┌─────────────────┐
                    │ 通知服务        │    │ 公共模块        │
                    │ (端口:8085)     │    │                 │
                    └─────────────────┘    └─────────────────┘
```

## 服务模块

### 1. API网关 (shared-room-gateway)
- **端口**: 8080
- **功能**: 统一入口、路由转发、JWT认证、限流熔断
- **技术**: Spring Cloud Gateway, JWT

### 2. 用户服务 (shared-room-user)
- **端口**: 8081
- **功能**: 用户注册、登录、个人信息管理、JWT生成验证
- **数据库**: shared_room_user

### 3. 座位服务 (shared-room-seat)
- **端口**: 8082
- **功能**: 自习室管理、座位管理、地理位置搜索、分布式锁防超卖
- **数据库**: shared_room_seat
- **技术**: Redis分布式锁、地理位置计算

### 4. 订单服务 (shared-room-order)
- **端口**: 8083
- **功能**: 订单创建、状态管理、分布式事务
- **数据库**: shared_room_order
- **技术**: Seata分布式事务

### 5. 支付服务 (shared-room-payment)
- **端口**: 8084
- **功能**: 支付宝集成、支付回调处理、退款管理
- **数据库**: shared_room_payment
- **技术**: 支付宝SDK

### 6. 通知服务 (shared-room-notification)
- **端口**: 8085
- **功能**: WebSocket实时推送、邮件通知、短信通知、消息队列处理
- **数据库**: shared_room_notification
- **技术**: WebSocket, RocketMQ, 邮件服务

### 7. 公共模块 (shared-room-common)
- **功能**: 实体类、工具类、异常处理、统一响应格式

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.0+
- Nacos 2.1.0+
- RocketMQ 4.9.4+

### 1. 克隆项目
```bash
git clone <repository-url>
cd shared-room
```

### 2. 数据库初始化
创建以下数据库：
```sql
CREATE DATABASE shared_room_user;
CREATE DATABASE shared_room_seat;
CREATE DATABASE shared_room_order;
CREATE DATABASE shared_room_payment;
CREATE DATABASE shared_room_notification;
```

### 3. 启动基础服务

#### 启动Nacos
```bash
# 下载Nacos 2.1.0
# 启动Nacos
sh startup.sh -m standalone
```
访问: http://localhost:8848/nacos (用户名/密码: nacos/nacos)

#### 启动Redis
```bash
redis-server
```

#### 启动RocketMQ
```bash
# 启动NameServer
sh mqnamesrv

# 启动Broker
sh mqbroker -n localhost:9876
```

### 4. 编译项目
```bash
mvn clean compile
```

### 5. 启动服务
按以下顺序启动各个服务：

```bash
# 1. 启动公共模块（安装到本地仓库）
cd shared-room-common
mvn clean install

# 2. 启动网关服务
cd ../shared-room-gateway
mvn spring-boot:run

# 3. 启动用户服务
cd ../shared-room-user
mvn spring-boot:run

# 4. 启动座位服务
cd ../shared-room-seat
mvn spring-boot:run

# 5. 启动订单服务
cd ../shared-room-order
mvn spring-boot:run

# 6. 启动支付服务
cd ../shared-room-payment
mvn spring-boot:run

# 7. 启动通知服务
cd ../shared-room-notification
mvn spring-boot:run
```

### 6. 验证服务
访问Nacos控制台确认所有服务已注册：http://localhost:8848/nacos

## API文档

### 用户服务 API

#### 用户注册
```http
POST /api/users/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

#### 用户登录
```http
POST /api/users/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

### 座位服务 API

#### 搜索自习室
```http
GET /api/seats/rooms/search?longitude=116.4074&latitude=39.9042&radius=5000
```

#### 获取座位列表
```http
GET /api/seats/list?roomId=1&status=1
```

#### 预订座位
```http
POST /api/seats/reserve
Content-Type: application/json
Authorization: Bearer <token>

{
  "seatId": 1,
  "startTime": "2024-01-01 09:00:00",
  "endTime": "2024-01-01 17:00:00"
}
```

### 订单服务 API

#### 创建订单
```http
POST /api/orders/create
Content-Type: application/json
Authorization: Bearer <token>

{
  "seatId": 1,
  "startTime": "2024-01-01 09:00:00",
  "endTime": "2024-01-01 17:00:00"
}
```

#### 查询订单
```http
GET /api/orders/my?page=1&size=10
Authorization: Bearer <token>
```

### 支付服务 API

#### 创建支付
```http
POST /api/payments/create
Content-Type: application/json
Authorization: Bearer <token>

{
  "orderId": 1,
  "paymentMethod": "ALIPAY"
}
```

#### 查询支付状态
```http
GET /api/payments/status/{paymentNo}
Authorization: Bearer <token>
```

### 通知服务 API

#### 获取通知列表
```http
GET /api/notifications/my?page=1&size=10
Authorization: Bearer <token>
```

#### 标记已读
```http
PUT /api/notifications/{id}/read
Authorization: Bearer <token>
```

## 配置说明

### Nacos配置
各服务的配置文件存储在Nacos配置中心，命名空间为`shared-room`。

### 数据库配置
各服务使用独立的数据库，连接信息在各自的`application.yml`中配置。

### Redis配置
- 用户服务使用database 0
- 座位服务使用database 1
- 订单服务使用database 2
- 通知服务使用database 3

### 支付配置
支付宝相关配置需要在`shared-room-payment`的`application.yml`中配置：
```yaml
payment:
  alipay:
    app-id: your-app-id
    private-key: your-private-key
    public-key: alipay-public-key
    gateway-url: https://openapi.alipay.com/gateway.do
```

## 监控与运维

### 健康检查
各服务提供健康检查端点：
```http
GET /actuator/health
```

### 服务监控
通过Nacos控制台可以查看服务状态和实例信息。

### 日志管理
各服务日志级别可通过配置文件调整，支持动态刷新。

## 开发指南

### 代码规范
- 使用统一的代码格式化规则
- 遵循RESTful API设计规范
- 使用统一的异常处理机制
- 添加必要的注释和文档

### 测试
```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

### 打包部署
```bash
# 打包所有模块
mvn clean package

# 打包单个模块
cd shared-room-gateway
mvn clean package
```

## 常见问题

### Q: 服务启动失败
A: 检查以下几点：
1. 确保Nacos、MySQL、Redis等基础服务已启动
2. 检查端口是否被占用
3. 检查数据库连接配置是否正确
4. 查看服务日志获取详细错误信息

### Q: 服务间调用失败
A: 检查以下几点：
1. 确保服务已在Nacos中注册
2. 检查网络连接是否正常
3. 检查JWT token是否有效
4. 查看网关和目标服务的日志

### Q: 支付回调失败
A: 检查以下几点：
1. 确保回调地址可以从外网访问
2. 检查支付宝配置是否正确
3. 查看支付服务日志

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 邮箱: developer@example.com
- 项目地址: https://github.com/your-username/shared-room

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 实现用户管理、座位预订、订单支付、实时通知等核心功能
- 支持微服务架构和分布式部署