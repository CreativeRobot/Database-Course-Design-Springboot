# Flyway Schema Validation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Move the bookstore schema to Flyway-managed MySQL migrations, add the missing `book.sales_count` column, and prove a blank MySQL database migrates successfully before Hibernate validates every entity mapping.

**Architecture:** Flyway is the sole source of schema evolution under `src/main/resources/db/migration`. Spring Boot executes migrations before JPA starts; JPA remains at `ddl-auto=validate` to detect mapping drift. A MySQL Testcontainers integration test boots a blank database, runs the migrations, validates the Spring context, and asserts key schema artifacts.

**Tech Stack:** Spring Boot 4.1, Spring Data JPA, MySQL 8, Flyway, JUnit 5, Testcontainers.

## Global Constraints
- Preserve `spring.jpa.hibernate.ddl-auto=validate`; never use Hibernate `update` or `create` for application schema management.
- Do not retain a second authoritative schema definition outside Flyway.
- Use SQL versioned migrations and MySQL-specific Flyway support.
- The new migration test must use a real MySQL Testcontainer and start from an empty database.
- Leave the user-uploaded `uploads/avatars/22/` file untracked.

### Task 1: Add a failing blank-database migration validation test
**Files:**
- Create: `src/test/java/com/example/demo/DatabaseMigrationValidationTests.java`
- Create: `src/test/resources/application-test.properties`

- [ ] Add a Spring Boot + MySQL Testcontainers test that asserts `book.sales_count` exists after application startup.
- [ ] Run it before adding Flyway; expected result: context/dependency failure because migrations and Flyway dependency do not exist.

### Task 2: Introduce Flyway and establish the schema baseline
**Files:**
- Modify: `pom.xml`
- Create: `src/main/resources/db/migration/V1__create_initial_schema.sql`
- Modify: `src/main/resources/application.properties`
- Modify: `sql/README.md`

- [ ] Add Spring Boot Flyway and MySQL Flyway dependencies.
- [ ] Convert the current authoritative schema into a baseline migration, including `avatar_url` and `sales_count`.
- [ ] Configure Flyway and keep Hibernate validate.
- [ ] Make `sql/README.md` identify Flyway migrations as the schema source of truth.

### Task 3: Verify blank migration and entity validation
**Files:**
- Modify: `src/test/java/com/example/demo/DatabaseMigrationValidationTests.java` as needed

- [ ] Run the migration test; expected result: migrations complete and the JPA context validates.
- [ ] Assert the Flyway history contains V1 and the `book.sales_count` column is a non-null BIGINT with default 0.
- [ ] Run existing targeted repository tests to ensure the migration change did not alter stock behavior.

### Task 4: Final verification and commit
- [ ] Run `git diff --check`.
- [ ] Run Maven targeted tests and record Docker/environment limitations if applicable.
- [ ] Update plan/progress records and commit the Flyway implementation separately.
