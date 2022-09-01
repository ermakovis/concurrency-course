package course.concurrency.m3_shared.immutable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

public class OrderService {
    private Map<Long, Order> currentOrders = new ConcurrentHashMap<>();
    private AtomicLong nextId = new AtomicLong(0);

    private long nextId() {
        return nextId.getAndIncrement();
    }

    public long createOrder(List<Item> items) {
        long id = nextId();
        Order order = Order.builder()
                .id(id)
                .items(items)
                .build();
        currentOrders.put(id, order);
        return id;
    }

    public void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
        updateOrder(orderId, o -> o.withPaymentInfo(paymentInfo));
        tryToDeliver(orderId);
    }

    public void setPacked(long orderId) {
        updateOrder(orderId, order -> order.withPacked(true));
        tryToDeliver(orderId);
    }

    private void deliver(Order order) {
        /* ... */
        updateOrder(order.getId(), o -> o.withStatus(Order.Status.DELIVERED));
    }

    public boolean isDelivered(long orderId) {
        return currentOrders.get(orderId).getStatus().equals(Order.Status.DELIVERED);
    }

    private Order updateOrder(long orderId, UnaryOperator<Order> updater) {
        return currentOrders.compute(orderId, (k,v) -> updater.apply(v));
    }

    private void tryToDeliver(long orderId) {
        currentOrders.compute(orderId, (k,v) -> {
            Order order = currentOrders.get(orderId);
            return order.checkStatus() ? order.withStatus(Order.Status.DELIVERED) : order;
        });
    }
}
