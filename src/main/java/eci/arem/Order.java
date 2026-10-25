package eci.arem;

import java.util.Objects;
import java.util.UUID;

public record Order(String id, String customerId, String product, int quantity) {

    public Order(String customerId, String product, int quantity) {
        this(UUID.randomUUID().toString(), customerId, product, quantity);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Order order)) return false;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}