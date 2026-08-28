# Bookstore Database Scripts

## Schema source of truth

The application schema is managed by Flyway migrations in:

```text
src/main/resources/db/migration/
```

On application startup, Flyway applies all unapplied migrations and Hibernate then validates the entity mappings with `spring.jpa.hibernate.ddl-auto=validate`. Do not use Hibernate `create`, `create-drop`, or `update` to create the application schema.

For a fresh local database, create the database once, configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`, then start the application:

```sql
CREATE DATABASE bookstore
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
```

The baseline migration `V1__create_initial_schema.sql` includes `users.avatar_url`; the incremental `V2__add_book_sales_count.sql` adds and constrains `book.sales_count`. Flyway records applied versions in `flyway_schema_history`.

For a pre-existing non-empty database that has no Flyway history, the application records a V1 baseline (`spring.flyway.baseline-on-migrate=true`) and then runs V2. V2 checks the information schema first, so it preserves a legacy `sales_count` column if it already exists. Back up production databases before the first Flyway-enabled startup.

## Legacy and demonstration scripts

- `01_schema.sql` is a legacy, manual compatibility snapshot. New environments must use Flyway migrations as the authoritative schema source.
- `02_data.sql` optionally inserts classroom demonstration data after Flyway has created the schema.
- `03_queries.sql` contains optional demonstration queries.
- `04_test_orders_reviews.sql` contains optional order/review demonstration data.

The sample accounts inserted by `02_data.sql` are:

- `admin` / `password`
- `reader` / `password`

The sample password is only for local demonstration. Change it before using the application outside a classroom or local development environment.
