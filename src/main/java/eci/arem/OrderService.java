package eci.arem;

import eci.arem.dto.CreateOrder;
import eci.arem.dto.OrderMapper;
import eci.arem.exception.OrderErrorCode;
import eci.arem.exception.OrderException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class OrderService {
    private OrderService() {}

    private static final String[] CUSTOMERS = {"CUST-02", "CUST-03", "CUST-04", "CUST-05"};
    private static final String[] PRODUCTS = {"Laptop", "Monitor", "Teclado", "Mouse", "Impresora", "Router"};

    private static final Random random = new Random();
    private static final ArrayList<Order> orders = new ArrayList<>(generateRandomOrders());

    private static List<Order> generateRandomOrders(){
        List<Order> result = new ArrayList<>();


        for (int i = 0; i < 8; i++) {
            result.add(new Order(
                    "CUST-01",
                    PRODUCTS[random.nextInt(PRODUCTS.length)],
                    random.nextInt(10) + 1
            ));
        }

        for (int i = 0; i < 7; i++) {
            result.add(new Order(
                    CUSTOMERS[random.nextInt(CUSTOMERS.length)],
                    PRODUCTS[random.nextInt(PRODUCTS.length)],
                    random.nextInt(10) + 1
            ));
        }
        result.add(new Order("TEST-ORDER-001", "CUST-TEST", "Producto de prueba", 1));
        Collections.shuffle(result);
        return result;
    }


    public static Order addOrder(CreateOrder request) {
        Order order = OrderMapper.toEntity(request);
        orders.add(order);
        return order;
    }

    public static Order getOrderById(String id) {
        if(id == null || id.isBlank()) throw new OrderException(OrderErrorCode.BLANK_NULL);
        for(Order order : orders) {
            if(order.id().equals(id.trim())) return order;
        }
        throw new OrderException(OrderErrorCode.ORDER_NOT_FOUND);
    }

    public static List<Order> getOrdersByCustomerId(String customerId) {
        if(customerId == null || customerId.isBlank()) throw new OrderException(OrderErrorCode.BLANK_NULL);
        return orders.stream()
                .filter(order -> order.customerId().equals(customerId.trim()))
                .toList();
    }

    public static void deleteOrder(String id) {
        Order order = getOrderById(id);
        orders.remove(order);
    }
}