package com.example.demo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fast guard for the authoritative Flyway migrations. The MySQL Testcontainers
 * test verifies runtime execution when Docker is available.
 */
class FlywayMigrationResourceTests {

    @Test
    void migrationsSupportEmptyDatabasesAndExistingDatabasesWithoutHistory() throws IOException {
        String baseline = readClasspathResource("db/migration/V1__create_initial_schema.sql");
        String salesCountMigration = readClasspathResource("db/migration/V2__add_book_sales_count.sql");
        String consistencyMigration = readClasspathResource("db/migration/V3__enforce_business_consistency.sql");
        String applicationProperties = readClasspathResource("application.properties");

        assertTrue(!baseline.contains("CREATE DATABASE"));
        assertTrue(!baseline.contains("USE bookstore"));
        assertTrue(!baseline.contains("sales_count"));

        assertTrue(salesCountMigration.contains("sales_count BIGINT NOT NULL DEFAULT 0"));
        assertTrue(salesCountMigration.contains("information_schema.columns"));

        assertTrue(consistencyMigration.contains("default_user_id"));
        assertTrue(consistencyMigration.contains("uk_user_default_address"));
        assertTrue(consistencyMigration.contains("chk_order_discount_not_over_total"));
        assertTrue(consistencyMigration.contains("chk_order_payable_formula"));
        assertTrue(consistencyMigration.contains("chk_order_item_subtotal_formula"));
        assertTrue(consistencyMigration.contains("chk_inventory_stock_transition"));
        assertTrue(consistencyMigration.contains("chk_inventory_non_zero_change"));
        assertTrue(consistencyMigration.contains("chk_inventory_type_business_rules"));

        assertTrue(applicationProperties.contains("spring.flyway.baseline-on-migrate=true"));
        assertTrue(applicationProperties.contains("spring.flyway.baseline-version=1"));
    }

    private String readClasspathResource(String resourceName) throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(input, () -> "Required classpath resource must exist: " + resourceName);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
