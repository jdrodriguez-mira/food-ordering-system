package com.food.ordering.system.order.service.domain.mapper;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderDataMapper {

    public Restaurant createOrderCommandToRestaurant (CreateOrderCommand createOrderCommand){
        return Restaurant.Builder.builder()
                .id(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(createOrderCommand.getItems().stream().map(item -> new Product(new ProductId(item.getProductId()))).collect(Collectors.toList()))
                .build();
    }

    public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand){
        return Order.Builder.builder()
                .customerId(new CustomerId(createOrderCommand.getCustomerId()))
                .restaurantId(new RestaurantId((createOrderCommand.getRestaurantId())))
                .price(new Money(createOrderCommand.getPrice()))
                .deliveryAddress(mapOrderAddressToDeliveryAddress(createOrderCommand.getAddress()))
                .items(mapOrderItemsToOrderItemsEntities(createOrderCommand.getItems()))
                .build();
    }

    public CreateOrderResponse mapOrderToCreateOrderResponse(Order order) {
        return CreateOrderResponse.builder()
                .orderTrackingId(order.getTrackingId().getValue())
                .orderStatus(order.getOrderStatus())
                .build();
    }

    private List<OrderItem> mapOrderItemsToOrderItemsEntities(List<com.food.ordering.system.order.service.domain.dto.create.OrderItem> items) {
        return items.stream().map(orderItem -> mapOrderItemToOrderItemEntity(orderItem)).collect(Collectors.toList());
    }

    private OrderItem mapOrderItemToOrderItemEntity(com.food.ordering.system.order.service.domain.dto.create.OrderItem orderItem) {
        return OrderItem.Builder.builder()
                .product(new Product(new ProductId(orderItem.getProductId())))
                .quantity(orderItem.getQuantity().intValue())
                .price(new Money(orderItem.getPrice()))
                .subTotal(new Money(orderItem.getSubTotal()))
                .build();
    }

    private StreetAddress mapOrderAddressToDeliveryAddress(OrderAddress address) {
        return new StreetAddress(UUID.randomUUID(), address.getStreet(), address.getPostalCode(), address.getCity());
    }
}
