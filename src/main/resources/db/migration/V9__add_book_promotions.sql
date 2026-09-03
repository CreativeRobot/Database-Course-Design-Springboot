CREATE TABLE book_promotion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    discount_percent INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_book_promotion_book FOREIGN KEY (book_id) REFERENCES book(id),
    CONSTRAINT ck_book_promotion_discount CHECK (discount_percent BETWEEN 1 AND 99),
    CONSTRAINT ck_book_promotion_time CHECK (end_time > start_time)
);

CREATE INDEX idx_book_promotion_book_time ON book_promotion (book_id, start_time, end_time);
CREATE INDEX idx_book_promotion_status_time ON book_promotion (status, start_time, end_time);
