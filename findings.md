# Findings

- Existing cover upload endpoint: `POST /api/admin/uploads/images`, protected by the admin interceptor.
- `FileStorageService` currently validates image MIME types and size, generates UUID filenames, and stores files in `uploads/covers`.
- Static resources are exposed from `/uploads/**`.
- User profiles currently have no avatar field or endpoint.
- Existing untracked regression tests define `FileStorageService.storeAvatar(Long, MultipartFile)` and avatar URLs under `/uploads/avatars/{userId}/`.
- RED verification confirmed that `storeAvatar`, `updateAvatar`, and profile/avatar accessors were missing before implementation.
- Avatar storage and covers now share one image-validation and write path. The static `/uploads/**` handler already exposes both directories.

## 2026-08-20 Recommendation Continuation

- The approved recommendation design and execution plan live in the frontend repository under `docs/superpowers/`; both repositories are now on the requested `recommend` branch.
- The backend has uncommitted recommendation contracts, a ranking service, cache invalidation hooks, and focused tests. Continue from those files without discarding the user's in-progress work.
- The requested algorithm is a deterministic hybrid: category affinity from completed purchases and published ratings, item-based co-purchase affinity from completed orders, then popularity/rating/recency fallback. It needs no schema migration.

## 2026-08-23 Order Inventory Concurrency and Service Split

- The database-level conditional `UPDATE ... WHERE stock >= quantity` remains the concurrency boundary; the Java service does not use an in-memory lock.
- `InventoryService` now owns stock return plus cancellation inventory logs, and completed-order sales-count updates. The outer `@Transactional` order methods preserve atomic rollback across order state and inventory changes.
- The real concurrency test is intentionally MySQL-backed and is conditionally skipped only when Docker is unavailable; it is not replaced with H2 or Mockito.
## 2026-08-25 Recommendation Audit — Initial Scope
- Prior plan and test records indicate a recommendation module already exists with hybrid ranking, cache invalidation, JPA wiring, and focused tests (`RecommendationRankerTests`, `RecommendationServiceTests`, plus order/review tests).
- The working tree has unrelated tracked modifications in security/config/test files, so they are out of audit scope.
- `rg.exe` failed to start with an access-denied error in the sandbox; source inspection will use PowerShell commands.
- Repository Git ownership check is bypassed only per command with `git -c safe.directory=...`; no global configuration will be changed.

## 2026-08-25 Recommendation Audit — Code Evidence
- The implementation is a hybrid heuristic, not classical user-user/item-item collaborative filtering: it ranks a raw sum of category preference and historical co-purchase count; category preference is content-based, and the co-purchase term has no similarity/normalization calculation.
- Candidate generation loads every on-sale, in-stock book before all ranking work. It then fetches categories and average ratings for that entire candidate set and sorts it in memory; the `limit` is applied only after sorting.
- The co-purchase repository query self-joins `OrderItem` and aggregates `sum(sibling.quantity)`. It does not account for co-occurrence confidence, item popularity, user history recency, or order recency.
- Book publisher is LAZY but `RecommendationService.toVo` dereferences it for every returned result. The candidate repository methods do not fetch publisher, creating a potential one-query-per-distinct-publisher N+1 pattern (bounded by the output limit).
- Cache entries are per-process, keyed by `(userId, limit)`, expire after 15 minutes, and use a check-then-compute sequence. The invalidation call sites correctly defer to transaction commit and cover stock, catalogue, review, and completed-order changes, but all are process-local and most clear every user's cache.
- SQL schema lacks `book.sales_count`, although `Book` maps `salesCount` and popularity ranking reads it; with `spring.jpa.hibernate.ddl-auto=validate`, initializing only from `01_schema.sql` will not match the entity.
- Performance-relevant index gaps in the supplied schema: no `(user_id,status)` on `book_order`; no `(user_id,status)` or `(book_id,status)` coverage on `book_review`; no status/sales index for the eligible-popular-book query; and no reverse `(category_id,book_id)` index on `book_category`.
- Existing unit tests cover rank ordering, public-limit validation, batching calls, cache invalidation, and transactional invalidation. They do not cover SQL execution plans, cold-start/no-history diversity, score normalization, time decay, concurrent cache misses, multi-node cache coherence, or recommendation-quality metrics.
