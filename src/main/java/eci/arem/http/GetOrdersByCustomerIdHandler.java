package eci.arem.http;

import eci.arem.Order;
import eci.arem.OrderService;
import eci.arem.exception.OrderException;

import java.util.List;
import java.util.Map;

public class GetOrdersByCustomerIdHandler {

    public static Map<String, Object> handle(Map<String, Object> event) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> queryParams = (Map<String, String>) event.get("queryStringParameters");

            String customerId = queryParams != null ? queryParams.get("customerId") : null;

            List<Order> orders = OrderService.getOrdersByCustomerId(customerId);
            return HttpResponse.ok(200, orders);

        } catch (OrderException ex) {
            return HttpResponse.fromException(ex);
        } catch (Exception ex) {
            return HttpResponse.internalError(ex.getMessage());
        }
    }
}