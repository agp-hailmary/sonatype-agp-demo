package com.sonatype.demo.service;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Notification / messaging service that builds user-facing message templates.
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  DEMO SCENARIO — GOLDEN PR #3  /  REACHABLE VULNERABILITY           │
 * │                                                                     │
 * │  Library : commons-text 1.9                                         │
 * │  CVE     : CVE-2022-42889 (Text4Shell)   CVSS: 9.8  Critical        │
 * │  Fix     : commons-text 1.10.0  ← Golden PR suggestion             │
 * │                                                                     │
 * │  Why REACHABLE:                                                     │
 * │    processTemplate() calls StringSubstitutor.replace(template,...)  │
 * │    with a caller-controlled template string. This places the        │
 * │    vulnerable interpolation sink (script/dns/url lookups) directly  │
 * │    in the application call graph.                                   │
 * │                                                                     │
 * │  Policy action: FAIL build                                          │
 * └─────────────────────────────────────────────────────────────────────┘
 */
@Service
public class NotificationService {

    /**
     * Resolves a message template against a map of named variables.
     * Uses StringSubstitutor — makes CVE-2022-42889 REACHABLE.
     *
     * @param template   e.g. "Hello ${customer}, your order ${orderId} is ready."
     * @param variables  name → value substitution map
     */
    public String processTemplate(String template, Map<String, String> variables) {
        // ⚠️  StringSubstitutor.replace() — Text4Shell reachable sink
        StringSubstitutor substitutor = new StringSubstitutor(variables);
        return substitutor.replace(template);
    }

    /**
     * Builds a human-readable order-confirmation message.
     * Called by OrderService on every new order → confirms the reachable path is active.
     */
    public String buildOrderConfirmation(String customerName, String orderId, String productName) {
        Map<String, String> vars = Map.of(
            "customer", customerName,
            "orderId",  orderId,
            "product",  productName
        );
        return processTemplate(
            "Dear ${customer}, your order #${orderId} for \"${product}\" has been confirmed. Thank you!",
            vars
        );
    }

    /**
     * Builds a shipping notification message.
     */
    public String buildShippingNotification(String customerName, String trackingCode) {
        Map<String, String> vars = Map.of(
            "customer",     customerName,
            "trackingCode", trackingCode
        );
        return processTemplate(
            "Hi ${customer}, your package is on the way. Tracking: ${trackingCode}",
            vars
        );
    }
}
