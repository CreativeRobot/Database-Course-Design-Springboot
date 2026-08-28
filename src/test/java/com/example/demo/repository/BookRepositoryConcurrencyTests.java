package com.example.demo.repository;

import com.example.demo.entity.Book;
import com.example.demo.entity.BookStatus;
import com.example.demo.entity.InventoryChangeType;
import com.example.demo.entity.InventoryLog;
import com.example.demo.entity.Publisher;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Requires Docker. It exercises the real MySQL conditional UPDATE used by checkout.
 * Setups that do not have Docker skip this test instead of silently replacing MySQL
 * with an in-memory database.
 */
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@EnabledIf("dockerIsAvailable")
class BookRepositoryConcurrencyTests {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0")
            .withDatabaseName("bookstore_test")
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
    private BookRepository bookRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private InventoryLogRepository inventoryLogRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void concurrentDecrementWithOneStockAllowsExactlyOneWinner() throws Exception {
        Long bookId = transactionTemplate.execute(status -> {
            Publisher publisher = Publisher.builder().name("并发测试出版社").build();
            entityManager.persist(publisher);
            Book book = Book.builder()
                    .isbn("9780000000999")
                    .title("并发库存测试书")
                    .publisher(publisher)
                    .originalPrice(new BigDecimal("10.00"))
                    .salePrice(new BigDecimal("8.00"))
                    .stock(1)
                    .status(BookStatus.ON_SALE)
                    .build();
            entityManager.persist(book);
            entityManager.flush();
            return book.getId();
        });

        int workerCount = 2;
        CountDownLatch ready = new CountDownLatch(workerCount);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        try {
            Future<Integer> first = submitDecrement(executor, ready, start, bookId);
            Future<Integer> second = submitDecrement(executor, ready, start, bookId);
            assertEquals(true, ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            List<Integer> results = List.of(first.get(20, TimeUnit.SECONDS),
                    second.get(20, TimeUnit.SECONDS));

            assertEquals(1L, results.stream().filter(result -> result == 1).count());
            assertEquals(1L, results.stream().filter(result -> result == 0).count());
            Book persisted = bookRepository.findById(bookId).orElse(null);
            assertNotNull(persisted);
            assertEquals(0, persisted.getStock());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void concurrentStockChangesWriteExactInventoryTransitions() throws Exception {
        Long bookId = transactionTemplate.execute(status -> {
            Publisher publisher = Publisher.builder().name("流水并发测试出版社").build();
            entityManager.persist(publisher);
            Book book = Book.builder()
                    .isbn("9780000000998")
                    .title("并发流水精确性测试书")
                    .publisher(publisher)
                    .originalPrice(new BigDecimal("10.00"))
                    .salePrice(new BigDecimal("8.00"))
                    .stock(10)
                    .status(BookStatus.ON_SALE)
                    .build();
            entityManager.persist(book);
            entityManager.flush();
            return book.getId();
        });

        int workerCount = 10;
        CountDownLatch ready = new CountDownLatch(workerCount);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        try {
            List<Future<Integer>> workers = java.util.stream.IntStream.range(0, workerCount)
                    .mapToObj(index -> submitExactDecrement(executor, ready, start, bookId))
                    .toList();
            assertEquals(true, ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            List<Integer> results = workers.stream()
                    .map(future -> getFuture(future))
                    .toList();

            assertEquals(workerCount, results.stream().mapToInt(Integer::intValue).sum());
            entityManager.clear();
            Book persisted = bookRepository.findById(bookId).orElseThrow();
            List<InventoryLog> logs = inventoryLogRepository.findByBook_IdOrderByCreateTimeDesc(bookId,
                    org.springframework.data.domain.PageRequest.of(0, workerCount + 1)).getContent();

            assertEquals(workerCount, logs.size());
            assertEquals(0, persisted.getStock());
            assertEquals(0, 10 + logs.stream()
                    .mapToInt(InventoryLog::getChangeQuantity)
                    .sum());
            logs.forEach(log -> {
                assertEquals(log.getBeforeStock() + log.getChangeQuantity(), log.getAfterStock());
                assertEquals(InventoryChangeType.MANUAL_ADJUSTMENT, log.getChangeType());
                assertEquals(-1, log.getChangeQuantity());
            });
        } finally {
            executor.shutdownNow();
        }
    }

    private Future<Integer> submitExactDecrement(
            ExecutorService executor,
            CountDownLatch ready,
            CountDownLatch start,
            Long bookId) {
        return executor.submit(() -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("并发测试未能同时开始");
            }
            return transactionTemplate.execute(status -> {
                BookStockSnapshot snapshot = bookRepository.findStockSnapshotForUpdate(bookId).orElseThrow();
                if (snapshot.getStock() < 1) {
                    return 0;
                }
                int beforeStock = snapshot.getStock();
                int affectedRows = bookRepository.decreaseStock(bookId, 1, BookStatus.ON_SALE);
                if (affectedRows != 1) {
                    return 0;
                }
                inventoryLogRepository.saveAndFlush(InventoryLog.builder()
                        .book(bookRepository.getReferenceById(bookId))
                        .changeQuantity(-1)
                        .beforeStock(beforeStock)
                        .afterStock(beforeStock - 1)
                        .changeType(InventoryChangeType.MANUAL_ADJUSTMENT)
                        .remark("并发测试")
                        .build());
                return 1;
            });
        });
    }

    private int getFuture(Future<Integer> future) {
        try {
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new AssertionError("并发任务执行失败", exception);
        }
    }

    private Future<Integer> submitDecrement(
            ExecutorService executor,
            CountDownLatch ready,
            CountDownLatch start,
            Long bookId) {
        return executor.submit(() -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("并发测试未能同时开始");
            }
            return transactionTemplate.execute(status ->
                    bookRepository.decreaseStock(bookId, 1, BookStatus.ON_SALE));
        });
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