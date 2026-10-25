package eci.arem.dto;

import eci.arem.Order;

public class OrderMapper {
    private OrderMapper() {}

    public static Order toEntity(CreateOrder request){
        return new Order(request.customerId(), request.product(), request.quantity());
    }
}
