package com.sonatype.demo.controller;

import com.sonatype.demo.model.Order;
import com.sonatype.demo.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoint for order management.
 *
 * Order creation chains into NotificationService.buildOrderConfirmation()
 * which calls StringSubstitutor.replace() — REACHABLE path for CVE-2022-42889 (Text4Shell).
 *
 * Demo tip:
 *   POST /api/orders
 *   {"productId": 1, "quantity": 2, "customerEmail": "user@example.com"}
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        // Triggers NotificationService → commons-text REACHABLE path
        return orderService.createOrder(order);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{email}")
    public List<Order> getOrdersByCustomer(@PathVariable String email) {
        return orderService.getOrdersByCustomer(email);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        return orderService.updateOrderStatus(id, body.get("status"))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
