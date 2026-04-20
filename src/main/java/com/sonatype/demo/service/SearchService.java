package com.sonatype.demo.service;

import com.sonatype.demo.model.Product;
import com.sonatype.demo.repository.ProductRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Search service for product catalog queries.
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  DEMO SCENARIO — GOLDEN PR #1  /  REACHABLE VULNERABILITY           │
 * │                                                                     │
 * │  Library : log4j-core 2.14.1                                        │
 * │  CVE     : CVE-2021-44228 (Log4Shell)   CVSS: 10.0  Critical        │
 * │  Fix     : log4j-core 2.17.2  ← Golden PR suggestion               │
 * │                                                                     │
 * │  Why REACHABLE:                                                     │
 * │    This class uses the Log4j API directly. The user-controlled      │
 * │    search query flows into logger.info() and logger.debug(),        │
 * │    placing the vulnerable JNDI lookup sink (MessagePatternConverter) │
 * │    squarely in the application call graph.                          │
 * │                                                                     │
 * │  Policy action: FAIL build                                          │
 * └─────────────────────────────────────────────────────────────────────┘
 */
@Service
public class SearchService {

    // Using Log4j API — makes CVE-2021-44228 REACHABLE via Sonatype Reachability Analysis
    private static final Logger logger = LogManager.getLogger(SearchService.class);

    private final ProductRepository productRepository;

    public SearchService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Full-text search on product names.
     * User-supplied query is logged via Log4j — this is the REACHABLE path.
     */
    public List<Product> search(String query) {
        // ⚠️  User input flows directly into Log4j logger — Log4Shell reachable sink
        logger.info("Processing search query: {}", query);
        logger.debug("Full-text search initiated for term: " + query);
        return productRepository.findByNameContainingIgnoreCase(query);
    }

    /**
     * Category-based search.
     */
    public List<Product> searchByCategory(String category) {
        logger.info("Searching products in category: {}", category);
        return productRepository.findByCategory(category);
    }
}
