-- Supports both a blank database (after V1) and a pre-Flyway legacy schema.
DELIMITER $$

CREATE PROCEDURE flyway_add_book_sales_count()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'book'
          AND column_name = 'sales_count'
    ) THEN
        ALTER TABLE book
            ADD COLUMN sales_count BIGINT NOT NULL DEFAULT 0 AFTER stock;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_schema = DATABASE()
          AND table_name = 'book'
          AND constraint_name = 'chk_book_sales_count'
    ) THEN
        ALTER TABLE book
            ADD CONSTRAINT chk_book_sales_count CHECK (sales_count >= 0);
    END IF;
END$$

CALL flyway_add_book_sales_count()$$
DROP PROCEDURE flyway_add_book_sales_count$$

DELIMITER ;
