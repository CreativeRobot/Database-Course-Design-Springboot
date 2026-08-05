# Bookstore Database Scripts

These scripts target MySQL 8.0 and should be executed in the following order:

1. `01_schema.sql`
2. `02_data.sql`
3. `03_queries.sql` (optional demonstration queries)

The sample accounts inserted by `02_data.sql` are:

- `admin` / `password`
- `reader` / `password`

The sample password is only for local demonstration. Change it before using
the application outside a classroom or local development environment.

After importing `01_schema.sql`, the application can use:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

This makes Hibernate validate the entity mapping without changing the schema.

`book_order.expire_time` is the payment deadline for pending orders. The
schema also includes an index on `(status, expire_time)` for the expiration
scanner.
