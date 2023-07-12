package com.food.ordering.system.order.service.dataaccess.customer.mapper;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.order.service.dataaccess.customer.entity.CustomerEntity;
import com.food.ordering.system.order.service.domain.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataAccessMapper {
    public Customer customerEntityToCustomer(CustomerEntity customerEntity){
        Customer customer = new Customer();
        customer.setId(new CustomerId(customerEntity.getCustomerId()));
        return customer;
    }

    public CustomerEntity customerToCustomerEntity(Customer customer){
        return CustomerEntity.builder().customerId(customer.getId().getValue()).build();
    }
}
