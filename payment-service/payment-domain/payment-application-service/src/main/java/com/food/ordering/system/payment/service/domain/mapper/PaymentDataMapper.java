package com.food.ordering.system.payment.service.domain.mapper;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.valueobject.PaymentId;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentDataMapper {
    public Payment PaymentRequestToPayment(PaymentRequest paymentRequest) {
        return Payment.Builder.newBuilder()
                .orderId(new OrderId(UUID.fromString(paymentRequest.getId())))
                .customerId(new CustomerId(UUID.fromString(paymentRequest.getId())))
                .price(new Money(paymentRequest.getPrice()))
                .build();
    }
}
