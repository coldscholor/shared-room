package com.sharedroom.payment.feign;

import com.sharedroom.common.result.Result;
import com.sharedroom.payment.vo.OrderVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单服务Feign客户端
 */
@FeignClient(name = "shared-room-order", path = "/api/orders")
public interface OrderFeignClient {

    /**
     * 根据ID获取订单详情
     */
    @GetMapping("/{orderId}")
    Result<OrderVO> getOrderById(@PathVariable("orderId") Long orderId);

    /**
     * 支付订单
     */
    @PutMapping("/{orderId}/pay")
    Result<Boolean> payOrder(@PathVariable("orderId") Long orderId,
                            @RequestParam("payMethod") String payMethod,
                            @RequestParam("payTransactionId") String payTransactionId);

    /**
     * 取消订单
     */
    @PutMapping("/{orderId}/cancel")
    Result<Boolean> cancelOrder(@PathVariable("orderId") Long orderId,
                               @RequestParam(value = "cancelReason", required = false) String cancelReason);
}