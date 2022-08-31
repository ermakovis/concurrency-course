package course.concurrency.m3_shared.immutable;

import lombok.Builder;

import java.util.Collections;
import java.util.List;

import static course.concurrency.m3_shared.immutable.Order.Status.IN_PROGRESS;
import static course.concurrency.m3_shared.immutable.Order.Status.NEW;

@Builder
public final class Order {

    public enum Status { NEW, IN_PROGRESS, DELIVERED }

    private final Long id;
    private final List<Item> items;
    private final PaymentInfo paymentInfo;
    private final boolean isPacked;
    private final Status status;

    public Order(List<Item> items) {
        this.id = null;
        this.paymentInfo = null;
        this.isPacked = false;
        this.items = Collections.unmodifiableList(items);
        this.status = NEW;
    }

    public boolean checkStatus() {
        return !items.isEmpty() && paymentInfo != null && isPacked;
    }

    public Long getId() {
        return id;
    }

    public Order withId(Long id) {
        return Order.builder()
                .id(id)
                .items(this.items)
                .paymentInfo(this.paymentInfo)
                .isPacked(this.isPacked)
                .status(this.status)
                .build();
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public Order withPaymentInfo(PaymentInfo paymentInfo) {
        return Order.builder()
                .id(this.id)
                .items(this.items)
                .paymentInfo(paymentInfo)
                .isPacked(this.isPacked)
                .status(IN_PROGRESS)
                .build();
    }

    public boolean isPacked() {
        return isPacked;
    }

    public Order withPacked(boolean packed) {
        return Order.builder()
                .id(this.id)
                .items(this.items)
                .paymentInfo(this.paymentInfo)
                .isPacked(packed)
                .status(IN_PROGRESS)
                .build();
    }

    public Status getStatus() {
        return status;
    }

    public Order withStatus(Status status) {
        return Order.builder()
                .id(this.id)
                .items(this.items)
                .paymentInfo(this.paymentInfo)
                .isPacked(this.isPacked)
                .status(status)
                .build();
    }
}
