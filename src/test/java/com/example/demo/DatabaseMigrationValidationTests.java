package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies the production schema can be created from an empty MySQL database
 * before Hibernate validates the entity mappings.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@EnabledIf("dockerIsAvailable")
class DatabaseMigrationValidationTests {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bookstore_migration_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void migratesEmptyDatabaseBeforeHibernateValidatesMappings() {
        Integer salesCountColumn = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'book'
                  AND column_name = 'sales_count'
                """, Integer.class);

        assertEquals(1, salesCountColumn);

        Integer salesCountMigrationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE version = '2'
                  AND success = TRUE
                """, Integer.class);

        assertEquals(1, salesCountMigrationCount);

        Integer defaultUserIdColumn = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'user_address'
                  AND column_name = 'default_user_id'
                """, Integer.class);
        assertEquals(1, defaultUserIdColumn);

        Integer consistencyMigrationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE version = '3'
                  AND success = TRUE
                """, Integer.class);
        assertEquals(1, consistencyMigrationCount);
    }

    @Test
    void seedsBookFocusedCommunityDemoData() {
        Integer demoUsers = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM users
                WHERE username IN ('community_linan', 'community_muyu', 'community_xiaozhou', 'community_ake')
                """, Integer.class);
        assertEquals(4, demoUsers);

        Integer demoPosts = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM community_post
                WHERE title LIKE '【读书交流】%'
                """, Integer.class);
        assertEquals(10, demoPosts);

        Integer linkedBooks = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM community_post_book cpb
                JOIN community_post cp ON cp.id = cpb.post_id
                WHERE cp.title LIKE '【读书交流】%'
                """, Integer.class);
        assertEquals(14, linkedBooks);

        Integer postImages = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM community_post_image cpi
                JOIN community_post cp ON cp.id = cpi.post_id
                WHERE cp.title LIKE '【读书交流】%'
                """, Integer.class);
        assertEquals(9, postImages);

        Integer comments = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM community_comment cc
                JOIN community_post cp ON cp.id = cc.post_id
                WHERE cp.title LIKE '【读书交流】%'
                """, Integer.class);
        assertEquals(26, comments);

        Integer replies = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM community_comment cc
                JOIN community_post cp ON cp.id = cc.post_id
                WHERE cp.title LIKE '【读书交流】%'
                  AND cc.parent_id IS NOT NULL
                """, Integer.class);
        assertEquals(6, replies);
    }

    static boolean dockerIsAvailable() {
        try {
            Process process = new ProcessBuilder("docker", "version")
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception ignored) {
            return false;
        }
    }
}
