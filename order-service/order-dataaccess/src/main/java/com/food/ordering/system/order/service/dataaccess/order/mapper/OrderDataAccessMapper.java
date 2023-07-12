package com.food.ordering.system.order.service.dataaccess.order.mapper;

import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderAddressEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderItemEntity;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class OrderDataAccessMapper {

    public OrderEntity orderToOrderEntity(Order order){
        OrderEntity orderEntity = OrderEntity.builder()
                .id(order.getId().getValue())
                .customerId(order.getCustomerId().getValue())
                .restaurantId(order.getRestaurantId().getValue())
                .trackingId(order.getTrackingId().getValue())
                .address(deliveryAddressToAddressEntity(order.getDeliveryAddress()))
                .price(order.getPrice().getAmount())
                .items(orderItemsToOrderEntityItems(order.getItems()))
                .orderStatus(order.getOrderStatus())
                .failureMessages(Objects.nonNull(order.getFailureMessages()) ? String.join(",", order.getFailureMessages()) : "")
                .build();
        orderEntity.getAddress().setOrder(orderEntity);
        orderEntity.getItems().forEach(orderItemEntity -> orderItemEntity.setOrder(orderEntity));

        return orderEntity;
    }

    public Order orderEntityToOrder(OrderEntity orderEntity){
        return Order.Builder.builder()
                .id(new OrderId(orderEntity.getId()))
                .customerId(new CustomerId(orderEntity.getCustomerId()))
                .restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
                .orderStatus(orderEntity.getOrderStatus())
                .price(new Money(orderEntity.getPrice()))
                .tracking(new TrackingId(orderEntity.getTrackingId()))
                .deliveryAddress(addressEntityToDeliveryAddress(orderEntity.getAddress()))
                .items(orderEntityItemsToOrderItems(orderEntity.getItems()))
                .failureMessages(Arrays.stream(orderEntity.getFailureMessages().split(",")).toList())
                .build();
    }

    private List<OrderItemEntity> orderItemsToOrderEntityItems(List<OrderItem> items) {
        return items.stream().map(orderItem -> {
            return OrderItemEntity.builder()
                    .id(orderItem.getId().getValue())
                    .productId(orderItem.getProduct().getId().getValue())
                    .price(orderItem.getPrice().getAmount())
                    .quantity(orderItem.getQuantity())
                    .subTotal(orderItem.getSubTotal().getAmount())
                    .build();
        }).collect(Collectors.toList());
    }

    private List<OrderItem> orderEntityItemsToOrderItems(List<OrderItemEntity> items) {
        return items.stream().map(orderItem -> {
            return OrderItem.Builder.builder()
                    .orderItemId(new OrderItemId(orderItem.getId()))
                    .product(new Product(new ProductId(orderItem.getProductId())))
                    .price(new Money(orderItem.getPrice()))
                    .quantity(orderItem.getQuantity())
                    .subTotal(new Money(orderItem.getSubTotal()))
                    .build();
        }).collect(Collectors.toList());
    }

    private OrderAddressEntity deliveryAddressToAddressEntity(StreetAddress deliveryAddress) {
        return OrderAddressEntity.builder()
                .id(deliveryAddress.getId())
                .street(deliveryAddress.getStreet())
                .city(deliveryAddress.getCity())
                .postalCode(deliveryAddress.getPostalCode())
                .build();
    }

    private StreetAddress addressEntityToDeliveryAddress(OrderAddressEntity orderAddressEntity) {
        return new StreetAddress(
                orderAddressEntity.getId(),
                orderAddressEntity.getStreet(),
                orderAddressEntity.getPostalCode(),
                orderAddressEntity.getCity()
        );
    }
}
