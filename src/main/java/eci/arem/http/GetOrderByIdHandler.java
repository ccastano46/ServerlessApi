package eci.arem.http;

import eci.arem.Order;
import eci.arem.OrderService;
import eci.arem.exception.OrderException;

import java.util.Map;

public class GetOrderByIdHandler {

    public static Map<String, Object> handle(Map<String, Object> event) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> pathParameters = (Map<String, String>) event.get("pathParameters");

            String id = pathParameters != null ? pathParameters.get("id") : null;

            Order order = OrderService.getOrderById(id);
            return HttpResponse.ok(200, order);

        } catch (OrderException ex) {
            return HttpResponse.fromException(ex);
        } catch (Exception ex) {
            return HttpResponse.internalError(ex.getMessage());
        }
    }
}