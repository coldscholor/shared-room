package com.sharedroom.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 支付服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableTransactionManagement
@MapperScan("com.sharedroom.payment.mapper")
public class PaymentApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}