package com.sonatype.demo.controller;

import com.sonatype.demo.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoint for inventory information.
 *
 * Uses InventoryService which imports commons-collections 3.2.1 but only
 * calls the SAFE StringValueTransformer — CVE-2015-6420 is NOT REACHABLE.
 *
 * Demo tip: This is the Automated Waiver scenario.
 *   Sonatype Reachability Analysis marks it NOT REACHABLE.
 *   Configure an Automated Waiver rule to auto-suppress it,
 *   showing how developer toil drops without sacrificing security.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public Map<String, Integer> getStockLevels() {
        return inventoryService.getStockLevels();
    }

    @GetMapping("/category/{category}")
    public Map<String, Object> getCategoryStatus(@PathVariable String category) {
        boolean inStock = inventoryService.isCategoryInStock(category);
        String displayName = inventoryService.formatCategoryName(category);
        return Map.of(
            "category", displayName,
            "inStock",  inStock
        );
    }
}
