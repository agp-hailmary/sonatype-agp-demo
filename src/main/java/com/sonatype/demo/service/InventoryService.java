package com.sonatype.demo.service;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.StringValueTransformer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Inventory management service.
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  DEMO SCENARIO — GOLDEN PR #4  /  NOT REACHABLE → AUTOMATED WAIVER │
 * │                                                                     │
 * │  Library : commons-collections 3.2.1                               │
 * │  CVE     : CVE-2015-6420   CVSS: 7.5  High                         │
 * │  Fix     : commons-collections 3.2.2  ← Golden PR suggestion       │
 * │                                                                     │
 * │  Why NOT REACHABLE:                                                 │
 * │    The known exploit requires Java deserialization gadget chains    │
 * │    via InvokerTransformer chained with ConstantTransformer and      │
 * │    ChainedTransformer. None of those classes are ever instantiated  │
 * │    or invoked in this application.                                  │
 * │                                                                     │
 * │    This service only uses the SAFE StringValueTransformer, which    │
 * │    simply calls Object.toString() — no reflection, no deserialization│
 * │    chain, no ObjectInputStream involvement.                         │
 * │                                                                     │
 * │  Automated Waiver:                                                  │
 * │    Sonatype Reachability Analysis will mark this NOT REACHABLE.     │
 * │    An Automated Waiver can be configured to auto-suppress it,       │
 * │    reducing developer toil without compromising security posture.   │
 * └─────────────────────────────────────────────────────────────────────┘
 */
@Service
public class InventoryService {

    // Uses only the SAFE StringValueTransformer — InvokerTransformer is NEVER instantiated
    private final Transformer nameFormatter = StringValueTransformer.stringValueTransformer();

    /**
     * Returns current stock levels per product category.
     */
    public Map<String, Integer> getStockLevels() {
        Map<String, Integer> stock = new HashMap<>();
        stock.put("Electronics", 42);
        stock.put("Peripherals", 87);
        stock.put("Accessories", 134);
        stock.put("Software", 999);
        return stock;
    }

    /**
     * Formats a category name for display. Uses StringValueTransformer (safe).
     * NOT the vulnerable InvokerTransformer gadget.
     */
    public String formatCategoryName(Object categoryName) {
        // Safe transformer — just calls categoryName.toString()
        return (String) nameFormatter.transform(categoryName);
    }

    /**
     * Checks whether a product category is currently in stock.
     */
    public boolean isCategoryInStock(String category) {
        return getStockLevels().getOrDefault(category, 0) > 0;
    }
}
