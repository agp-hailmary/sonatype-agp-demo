package com.sonatype.demo.controller;

import com.sonatype.demo.model.Product;
import com.sonatype.demo.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoint for product search.
 *
 * User input flows:  HTTP request → search() → SearchService.search()
 *                    → logger.info(userQuery)  ← Log4Shell reachable sink
 *
 * Demo tip: Try GET /api/search?q=laptop
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Full-text product search.
     * User-supplied query is logged via Log4j — REACHABLE path for CVE-2021-44228.
     */
    @GetMapping
    public List<Product> search(@RequestParam String q) {
        return searchService.search(q);
    }

    /**
     * Category filter search.
     */
    @GetMapping("/category/{category}")
    public List<Product> searchByCategory(@PathVariable String category) {
        return searchService.searchByCategory(category);
    }
}
