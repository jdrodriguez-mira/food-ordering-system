package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Order extends AggregateRoot<OrderId> {
    private final CustomerId customerId;
    private final RestaurantId restaurantId;

    private final StreetAddress deliveryAddress;
    private Money price;
    private final List<OrderItem> items;

    private TrackingId trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessages;

    private Order(Builder builder) {
        super.setId(builder.orderId);
        customerId = builder.customerId;
        restaurantId = builder.restaurantId;
        deliveryAddress = builder.deliveryAddress;
        price = builder.price;
        items = builder.items;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        failureMessages = builder.failureMessages;
    }

    public void validateOrder(){
        validateInitialOrder();
        validateTotalPrice();
        validateItemsPrice();
    }

    private void validateInitialOrder() {
        if (Objects.nonNull(orderStatus) || Objects.nonNull(getId())){
            throw new OrderDomainException("Order is not in the correct state for initialization.");
        }
    }

    private void validateTotalPrice() {
        if (Objects.isNull(price) || !price.isGreaterThanZero()){
            throw new OrderDomainException("Price must be greater than zero.");
        }
    }

    private void validateItemsPrice() {
        Money totalPriceItems = items.stream().map(item -> {
            validateItemPrice(item);
            return item.getSubTotal();
        }).reduce(Money.ZERO, Money::add);

        if (!totalPriceItems.equals(price)){
            throw new OrderDomainException("Total price is not equals to the sum of the items");
        }
    }

    private void validateItemPrice(OrderItem item) {
        if (!item.isPriceValid()){
            throw new OrderDomainException("Item price is not valid.");
        }
    }

    public void pay() {
        if (!OrderStatus.PENDING.equals(this.orderStatus)){
            throw new OrderDomainException("Order is not in a valid status!.");
        }
        this.orderStatus = OrderStatus.PAID;
    }

    public void approve() {
        if (!OrderStatus.PAID.equals(this.orderStatus)){
            throw new OrderDomainException("Order is not in a valid status!.");
        }
        this.orderStatus = OrderStatus.APPROVED;
    }

    public void initCancel(List<String> failureMessages) {
        if (!OrderStatus.PAID.equals(this.orderStatus)){
            throw new OrderDomainException("Order is not in a valid status!.");
        }
        this.orderStatus = OrderStatus.CANCELLING;
        updateFailureMessages(failureMessages);
    }

    private void updateFailureMessages(List<String> failureMessages) {
        if (Objects.nonNull(failureMessages) && Objects.nonNull(this.failureMessages)){
            this.failureMessages.addAll(failureMessages.stream().filter(failureMessage -> !failureMessage.isEmpty()).collect(Collectors.toList()));
        }
        if (Objects.isNull(this.failureMessages)){
            this.failureMessages = failureMessages;
        }
    }

    public void cancel(List<String> failureMessages) {
        if (!(OrderStatus.CANCELLING.equals(this.orderStatus) || OrderStatus.PENDING.equals(this.orderStatus))){
            throw new OrderDomainException("Order is not in a valid status!.");
        }
        this.orderStatus = OrderStatus.CANCELLED;
        updateFailureMessages(failureMessages);
    }

    public void initializeOrder(){
        setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        initializeOrderItems();
    }
    private void initializeOrderItems(){
        long itemId = 1;
        for (OrderItem item : items){
            item.initializeOrderItem(super.getId(), itemId++);
        }
    }

    public static final class Builder {
        private OrderId orderId;
        private CustomerId customerId;
        private RestaurantId restaurantId;
        private StreetAddress deliveryAddress;
        private Money price;
        private List<OrderItem> items;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private List<String> failureMessages;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder id(OrderId orderId) {
            orderId = orderId;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder deliveryAddress(StreetAddress val) {
            deliveryAddress = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder items(List<OrderItem> val) {
            items = val;
            return this;
        }

        public Builder tracking(TrackingId trackingId) {
            trackingId = trackingId;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            failureMessages = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public StreetAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public Money getPrice() {
        return price;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public TrackingId getTrackingId() {
        return trackingId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }
}
