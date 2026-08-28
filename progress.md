# Flyway Schema Validation Progress

## 2026-08-28
- Existing feature work committed first as `476bbfb feat: enhance admin statistics and catalog management`.
- Implementation plan created. Next: write and run the failing empty-MySQL migration validation test.
- Added the RED migration-validation test and test profile. The test is expected to fail until Flyway migrations are added.
- Confirmed the Testcontainers test was skipped because Docker is unavailable. Added a fast Flyway migration-resource test to observe a local RED result before adding migration files.
- Reimplemented schema conversion with line-based PowerShell processing after the unsupported `\R` regex error. Flyway baseline, application configuration, legacy compatibility schema, and entity mapping are now written.
- Removed the obsolete avatar ALTER script so schema evolution has one documented authority: Flyway migrations.
- Verified `FlywayMigrationResourceTests,DatabaseMigrationValidationTests`: Maven build succeeded; 1 Flyway resource test passed and the 1 MySQL Testcontainers test was skipped because Docker is unavailable in this environment.
- Full `mvn test` initially failed in `SecurityCorsPreflightTests` and `DemoApplicationTests`: their populated local `bookstore` schema had no Flyway history table. Read-only inspection confirmed MySQL 8.0.39 and an already-present `book.sales_count`; next step is a V1 baseline plus idempotent V2 sales-count migration.
- Added `spring.flyway.baseline-on-migrate=true` at baseline version 1 and split `sales_count` into idempotent V2. The focused migration suite passes (the Testcontainers case is skipped without Docker). A full suite run successfully baselined the local development database and applied V2; its only remaining failure is the pre-existing `DemoApplicationTests` datasource placeholder configuration (`${DB_URL}` is unset).
