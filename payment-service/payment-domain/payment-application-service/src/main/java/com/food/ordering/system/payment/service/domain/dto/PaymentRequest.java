package com.food.ordering.system.payment.service.domain.dto;

import com.food.ordering.system.domain.valueobject.OrderPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class PaymentRequest {
    private String id;
    private String sagaId;
    private String orderId;
    private String customerId;
    private BigDecimal price;
    private Instant createdAt;
    private OrderPaymentStatus orderPaymentStatus;

    public void setOrderPaymentStatus(OrderPaymentStatus orderPaymentStatus) {
        this.orderPaymentStatus = orderPaymentStatus;
    }
}
