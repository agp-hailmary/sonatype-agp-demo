package com.sonatype.demo.service;

import com.sonatype.demo.model.Order;
import com.sonatype.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    /**
     * Creates a new order and sends a confirmation notification.
     * The notification call chains into NotificationService.buildOrderConfirmation(),
     * which calls StringSubstitutor.replace() — keeping the commons-text reachable path active.
     */
    public Order createOrder(Order order) {
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        Order saved = orderRepository.save(order);

        // Chains into NotificationService → commons-text REACHABLE path
        String confirmation = notificationService.buildOrderConfirmation(
            order.getCustomerEmail(),
            saved.getId().toString(),
            "Product #" + order.getProductId()
        );

        // In a real app this would be emailed; for the demo we just log it
        System.out.println("Order confirmation: " + confirmation);

        return saved;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByCustomer(String email) {
        return orderRepository.findByCustomerEmail(email);
    }

    public Optional<Order> updateOrderStatus(Long id, String status) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(status);
            return orderRepository.save(order);
        });
    }
}
