package com.sonatype.demo;

import com.sonatype.demo.model.Product;
import com.sonatype.demo.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the in-memory H2 database with demo product data on startup.
 *
 * Products span several categories so that all REST endpoints
 * return interesting data during a live demo:
 *   GET /api/products
 *   GET /api/search?q=laptop
 *   GET /api/inventory/category/electronics
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return; // already seeded
        }

        List<Product> products = List.of(
            product("Laptop Pro 15",       "High-performance laptop with 16 GB RAM",   new BigDecimal("1299.99"), "electronics"),
            product("Wireless Mouse",       "Ergonomic wireless mouse, 2.4 GHz",        new BigDecimal("29.99"),   "electronics"),
            product("USB-C Hub",            "7-in-1 USB-C hub with 4K HDMI",            new BigDecimal("49.99"),   "electronics"),
            product("Noise Cancelling Headphones", "Over-ear ANC headphones",           new BigDecimal("199.99"),  "electronics"),
            product("Mechanical Keyboard",  "TKL mechanical keyboard, Cherry MX",       new BigDecimal("89.99"),   "electronics"),
            product("Standing Desk",        "Electric height-adjustable standing desk", new BigDecimal("599.99"),  "furniture"),
            product("Ergonomic Chair",      "Mesh back ergonomic office chair",         new BigDecimal("449.99"),  "furniture"),
            product("Monitor Stand",        "Bamboo monitor riser with storage",        new BigDecimal("39.99"),   "furniture"),
            product("The Clean Coder",      "Robert C. Martin — professional software", new BigDecimal("34.99"),   "books"),
            product("Designing Data-Intensive Applications", "Martin Kleppmann",        new BigDecimal("49.99"),   "books"),
            product("Spring Boot in Action","Craig Walls — Spring Boot guide",          new BigDecimal("44.99"),   "books"),
            product("Coffee Mug",           "12 oz ceramic mug — \"Fix the bug\"",      new BigDecimal("14.99"),   "accessories"),
            product("Cable Management Kit", "Velcro ties + cable clips bundle",         new BigDecimal("9.99"),    "accessories"),
            product("Webcam 1080p",         "Full HD webcam with built-in mic",         new BigDecimal("79.99"),   "electronics"),
            product("Smart Speaker",        "Voice-controlled Bluetooth speaker",       new BigDecimal("99.99"),   "electronics")
        );

        productRepository.saveAll(products);
    }

    private Product product(String name, String description, BigDecimal price, String category) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setCategory(category);
        return p;
    }
}
