package eci.arem.dto;

import eci.arem.exception.OrderErrorCode;
import eci.arem.exception.OrderException;

public record CreateOrder(
        String customerId,
        String product,
        int quantity
) {
    public CreateOrder {
        if (quantity <= 0) throw  new OrderException(OrderErrorCode.NEGATIVE_QUANTITY);
        if (
                customerId == null || customerId.isBlank()
                || product == null || product.isBlank()
        ) throw new OrderException(OrderErrorCode.BLANK_NULL);
        customerId = customerId.trim();
        product = product.trim();
    }
}
