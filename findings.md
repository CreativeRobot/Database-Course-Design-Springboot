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