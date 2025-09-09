package com.sharedroom.order.config;

import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import org.springframework.context.annotation.Configuration;

/**
 * Seata分布式事务配置
 */
@Configuration
@EnableAutoDataSourceProxy
public class SeataConfig {
    // Seata会自动配置，这里只需要启用数据源代理
}