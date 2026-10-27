package eci.arem.http;

import eci.arem.Order;
import eci.arem.OrderService;
import eci.arem.dto.CreateOrder;
import eci.arem.exception.OrderErrorCode;
import eci.arem.exception.OrderException;

import java.util.Map;

public class CreateOrderHandler {

    public static Map<String, Object> handle(Map<String, Object> event) {
        try {
            String bodyString = (String) event.get("body");
            if (bodyString == null || bodyString.isBlank()) {
                return HttpResponse.fromException(
                        new OrderException(OrderErrorCode.BLANK_NULL)
                );
            }

            CreateOrder request = HttpResponse.MAPPER.readValue(bodyString, CreateOrder.class);
            Order created = OrderService.addOrder(request);
            return HttpResponse.created(created, "/orders/" + created.id());

        } catch (OrderException ex) {
            return HttpResponse.fromException(ex);
        } catch (com.fasterxml.jackson.databind.exc.ValueInstantiationException ex) {
            if (ex.getCause() instanceof OrderException oe) {
                return HttpResponse.fromException(oe);
            }
            return HttpResponse.internalError("Invalid request body");
        } catch (Exception ex) {
            return HttpResponse.internalError(ex.getMessage());
        }
    }
}