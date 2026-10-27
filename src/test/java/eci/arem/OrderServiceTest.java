package eci.arem;

import eci.arem.dto.CreateOrder;
import eci.arem.dto.OrderMapper;
import eci.arem.exception.OrderErrorCode;
import eci.arem.exception.OrderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del flujo de Order: limpieza de datos (DTO + mapper),
 * creación de órdenes y recuperación por id / customerId.
 */
class OrderServiceTest {

    // =================================================================
    // 1. Limpieza de datos (CreateOrder DTO + OrderMapper)
    // =================================================================
    @Nested
    @DisplayName("Limpieza de datos")
    class DataCleaning {

        @Test
        @DisplayName("Debe recortar espacios en customerId y product")
        void shouldTrimCustomerIdAndProduct() {
            CreateOrder request = new CreateOrder("  CUST-01  ", "  Laptop  ", 2);

            assertEquals("CUST-01", request.customerId());
            assertEquals("Laptop", request.product());
        }

        @Test
        @DisplayName("Debe lanzar BLANK_NULL cuando customerId es null")
        void shouldThrowWhenCustomerIdIsNull() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> new CreateOrder(null, "Laptop", 1));

            assertEquals(OrderErrorCode.BLANK_NULL, exception.getErrorCode());
        }

        @Test
        @DisplayName("Debe lanzar BLANK_NULL cuando customerId está en blanco")
        void shouldThrowWhenCustomerIdIsBlank() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> new CreateOrder("   ", "Laptop", 1));

            assertEquals(OrderErrorCode.BLANK_NULL, exception.getErrorCode());
        }

        @Test
        @DisplayName("Debe lanzar BLANK_NULL cuando product es null")
        void shouldThrowWhenProductIsNull() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> new CreateOrder("CUST-01", null, 1));

            assertEquals(OrderErrorCode.BLANK_NULL, exception.getErrorCode());
        }

        @Test
        @DisplayName("Debe lanzar BLANK_NULL cuando product está en blanco")
        void shouldThrowWhenProductIsBlank() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> new CreateOrder("CUST-01", "   ", 1));

            assertEquals(OrderErrorCode.BLANK_NULL, exception.getErrorCode());
        }

        @Test
        @DisplayName("Debe lanzar NEGATIVE_QUANTITY cuando quantity es 0")
        void shouldThrowWhenQuantityIsZero() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> new CreateOrder("CUST-01", "Laptop", 0));

            assertEquals(OrderErrorCode.NEGATIVE_QUANTITY, exception.getErrorCode());
        }

        @Test
        @DisplayName("Debe lanzar NEGATIVE_QUANTITY cuando quantity es negativo")
        void shouldThrowWhenQuantityIsNegative() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> new CreateOrder("CUST-01", "Laptop", -5));

            assertEquals(OrderErrorCode.NEGATIVE_QUANTITY, exception.getErrorCode());
        }

        @Test
        @DisplayName("OrderMapper debe mapear un CreateOrder ya limpio a Order sin alterar sus valores")
        void mapperShouldPreserveCleanedValues() {
            CreateOrder request = new CreateOrder("  CUST-07  ", "  Monitor  ", 3);

            Order order = OrderMapper.toEntity(request);

            assertNotNull(order.id());
            assertEquals("CUST-07", order.customerId());
            assertEquals("Monitor", order.product());
            assertEquals(3, order.quantity());
        }
    }

    // =================================================================
    // 2. Creación de orden (OrderService.addOrder)
    // =================================================================
    @Nested
    @DisplayName("Creación de orden")
    class CreateOrderOp {

        @Test
        @DisplayName("addOrder debe retornar la orden creada con id no nulo y los mismos datos insertados")
        void addOrderShouldReturnTheCreatedOrder() {
            String customerId = "CUST-ADD-" + System.nanoTime();
            CreateOrder request = new CreateOrder(customerId, "Teclado", 1);

            Order created = OrderService.addOrder(request);

            assertNotNull(created.id());
            assertEquals(customerId, created.customerId());
            assertEquals("Teclado", created.product());
            assertEquals(1, created.quantity());
        }

        @Test
        @DisplayName("addOrder debe dejar la orden disponible para consultarla luego")
        void addOrderShouldPersistTheOrder() {
            String customerId = "CUST-PERSIST-" + System.nanoTime();
            CreateOrder request = new CreateOrder(customerId, "Teclado", 1);

            Order created = OrderService.addOrder(request);

            List<Order> result = OrderService.getOrdersByCustomerId(customerId);
            assertEquals(1, result.size());
            assertEquals(created, result.get(0));
        }

        @Test
        @DisplayName("Cada orden creada recibe un id único")
        void eachCreatedOrderShouldHaveUniqueId() {
            String customerId = "CUST-UNIQUE-" + System.nanoTime();

            Order first = OrderService.addOrder(new CreateOrder(customerId, "Mouse", 1));
            Order second = OrderService.addOrder(new CreateOrder(customerId, "Mouse", 1));

            assertNotNull(first.id());
            assertNotNull(second.id());
            assertNotEquals(first.id(), second.id());
        }

        @Test
        @DisplayName("No debe poder crearse una orden con datos inválidos")
        void shouldNotCreateOrderWithInvalidData() {
            // La validación ocurre en el constructor compacto de CreateOrder,
            // por lo que addOrder nunca llega a invocarse con datos inválidos.
            assertThrows(OrderException.class,
                    () -> new CreateOrder("CUST-01", "Laptop", -1));
        }
    }

    // =================================================================
    // 3. Obtener por id y por customerId
    // =================================================================
    @Nested
    @DisplayName("Obtener orden por id y por customerId")
    class RetrieveOrders {

        @Test
        @DisplayName("getOrderById debe retornar la misma orden que fue creada")
        void getOrderByIdShouldReturnTheSameOrder() {
            String customerId = "CUST-GET-" + System.nanoTime();
            Order created = OrderService.addOrder(new CreateOrder(customerId, "Router", 2));

            Order fetched = OrderService.getOrderById(created.id());

            assertEquals(created, fetched);
            assertEquals(created.customerId(), fetched.customerId());
            assertEquals(created.product(), fetched.product());
            assertEquals(created.quantity(), fetched.quantity());
        }

        @Test
        @DisplayName("getOrderById debe lanzar ORDER_NOT_FOUND si el id no existe")
        void getOrderByIdShouldThrowWhenNotFound() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> OrderService.getOrderById("ID-QUE-NO-EXISTE"));

            assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("getOrderById debe lanzar BLANK_NULL cuando el id es null")
        void getOrderByIdShouldThrowWhenIdIsNull() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> OrderService.getOrderById(null));

            assertEquals(OrderErrorCode.BLANK_NULL, exception.getErrorCode());
        }

        @Test
        @DisplayName("getOrderById debe lanzar BLANK_NULL cuando el id está en blanco")
        void getOrderByIdShouldThrowWhenIdIsBlank() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> OrderService.getOrderById("   "));

            assertEquals(OrderErrorCode.BLANK_NULL, exception.getErrorCode());
        }

        @Test
        @DisplayName("getOrderById debe recortar espacios en el id antes de buscar")
        void getOrderByIdShouldTrimIdBeforeSearching() {
            String customerId = "CUST-TRIM-" + System.nanoTime();
            Order created = OrderService.addOrder(new CreateOrder(customerId, "Router", 1));

            Order fetched = OrderService.getOrderById("  " + created.id() + "  ");

            assertEquals(created, fetched);
        }

        @Test
        @DisplayName("getOrdersByCustomerId debe retornar todas las órdenes del cliente")
        void getOrdersByCustomerIdShouldReturnAllMatches() {
            String customerId = "CUST-MULTI-" + System.nanoTime();
            OrderService.addOrder(new CreateOrder(customerId, "Mouse", 1));
            OrderService.addOrder(new CreateOrder(customerId, "Monitor", 2));
            OrderService.addOrder(new CreateOrder(customerId, "Impresora", 3));

            List<Order> result = OrderService.getOrdersByCustomerId(customerId);

            assertEquals(3, result.size());
            assertTrue(result.stream().allMatch(o -> o.customerId().equals(customerId)));
        }

        @Test
        @DisplayName("getOrdersByCustomerId debe retornar lista vacía cuando el cliente no tiene órdenes")
        void getOrdersByCustomerIdShouldReturnEmptyWhenNoMatch() {
            List<Order> result = OrderService.getOrdersByCustomerId("CUST-NO-EXISTE-" + System.nanoTime());

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getOrdersByCustomerId debe lanzar BLANK_NULL cuando customerId es null")
        void getOrdersByCustomerIdShouldThrowWhenCustomerIdIsNull() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> OrderService.getOrdersByCustomerId(null));

            assertEquals(OrderErrorCode.BLANK_NULL, exception.getErrorCode());
        }

        @Test
        @DisplayName("getOrdersByCustomerId debe lanzar BLANK_NULL cuando customerId está en blanco")
        void getOrdersByCustomerIdShouldThrowWhenCustomerIdIsBlank() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> OrderService.getOrdersByCustomerId("   "));

            assertEquals(OrderErrorCode.BLANK_NULL, exception.getErrorCode());
        }

        @Test
        @DisplayName("getOrdersByCustomerId debe recortar espacios en customerId antes de buscar")
        void getOrdersByCustomerIdShouldTrimCustomerIdBeforeSearching() {
            String customerId = "CUST-TRIM-LIST-" + System.nanoTime();
            OrderService.addOrder(new CreateOrder(customerId, "Mouse", 1));

            List<Order> result = OrderService.getOrdersByCustomerId("  " + customerId + "  ");

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Dos órdenes distintas con los mismos datos no deben ser iguales (equals por id)")
        void twoDifferentOrdersWithSameDataShouldNotBeEqual() {
            String customerId = "CUST-EQ-" + System.nanoTime();
            Order first = OrderService.addOrder(new CreateOrder(customerId, "Laptop", 1));
            Order second = OrderService.addOrder(new CreateOrder(customerId, "Laptop", 1));

            assertNotEquals(first, second);
        }
    }

    // =================================================================
    // 4. Eliminación de orden
    // =================================================================
    @Nested
    @DisplayName("Eliminación de orden")
    class DeleteOrderOp {

        @Test
        @DisplayName("deleteOrder debe eliminar la orden existente")
        void deleteOrderShouldRemoveExistingOrder() {
            String customerId = "CUST-DEL-" + System.nanoTime();
            Order created = OrderService.addOrder(new CreateOrder(customerId, "Laptop", 1));

            OrderService.deleteOrder(created.id());

            OrderException exception = assertThrows(OrderException.class,
                    () -> OrderService.getOrderById(created.id()));
            assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("deleteOrder debe lanzar ORDER_NOT_FOUND si el id no existe")
        void deleteOrderShouldThrowWhenIdDoesNotExist() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> OrderService.deleteOrder("ID-QUE-NO-EXISTE"));

            assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("deleteOrder debe lanzar BLANK_NULL cuando el id es null")
        void deleteOrderShouldThrowWhenIdIsNull() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> OrderService.deleteOrder(null));

            assertEquals(OrderErrorCode.BLANK_NULL, exception.getErrorCode());
        }

        @Test
        @DisplayName("deleteOrder debe lanzar BLANK_NULL cuando el id está en blanco")
        void deleteOrderShouldThrowWhenIdIsBlank() {
            OrderException exception = assertThrows(OrderException.class,
                    () -> OrderService.deleteOrder("   "));

            assertEquals(OrderErrorCode.BLANK_NULL, exception.getErrorCode());
        }

        @Test
        @DisplayName("deleteOrder no debe afectar otras órdenes del mismo cliente")
        void deleteOrderShouldNotAffectOtherOrders() {
            String customerId = "CUST-DEL-MULTI-" + System.nanoTime();
            Order first = OrderService.addOrder(new CreateOrder(customerId, "Laptop", 1));
            Order second = OrderService.addOrder(new CreateOrder(customerId, "Mouse", 2));

            OrderService.deleteOrder(first.id());

            List<Order> remaining = OrderService.getOrdersByCustomerId(customerId);
            assertEquals(1, remaining.size());
            assertEquals(second, remaining.get(0));
        }
    }
}