-- Bookstore schema for MySQL 8.0+
-- This script is intentionally non-destructive: it does not drop existing data.

CREATE DATABASE IF NOT EXISTS bookstore
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE bookstore;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(30) NOT NULL,
    password VARCHAR(100) NOT NULL,
    status INT NOT NULL DEFAULT 1,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    role ENUM('ADMIN', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    nickname VARCHAR(30) NULL,
    email VARCHAR(100) NULL,
    phone VARCHAR(20) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT chk_users_status CHECK (status IN (0, 1))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS publisher (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NULL,
    address VARCHAR(255) NULL,
    introduction VARCHAR(500) NULL,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_publisher_name UNIQUE (name)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS author (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    country VARCHAR(50) NULL,
    introduction VARCHAR(1000) NULL,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_category_name UNIQUE (name),
    KEY idx_category_parent (parent_id),
    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id) REFERENCES category (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_category_sort_order CHECK (sort_order >= 0),
    CONSTRAINT chk_category_status CHECK (status IN (0, 1))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS book (
    id BIGINT NOT NULL AUTO_INCREMENT,
    isbn VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    publisher_id BIGINT NOT NULL,
    original_price DECIMAL(10, 2) NOT NULL,
    sale_price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    publish_date DATE NULL,
    edition VARCHAR(30) NULL,
    pages INT NULL,
    description TEXT NULL,
    cover_url VARCHAR(500) NULL,
    status ENUM('ON_SALE', 'OFF_SALE') NOT NULL DEFAULT 'ON_SALE',
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_book_isbn UNIQUE (isbn),
    KEY idx_book_title (title),
    KEY idx_book_publisher (publisher_id),
    CONSTRAINT fk_book_publisher
        FOREIGN KEY (publisher_id) REFERENCES publisher (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_book_original_price CHECK (original_price >= 0),
    CONSTRAINT chk_book_sale_price CHECK (sale_price >= 0),
    CONSTRAINT chk_book_price_order CHECK (sale_price <= original_price),
    CONSTRAINT chk_book_stock CHECK (stock >= 0),
    CONSTRAINT chk_book_pages CHECK (pages IS NULL OR pages > 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS book_author (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    author_order INT NOT NULL DEFAULT 1,
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_book_author_book
        FOREIGN KEY (book_id) REFERENCES book (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_book_author_author
        FOREIGN KEY (author_id) REFERENCES author (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_book_author_order CHECK (author_order > 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS book_category (
    book_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, category_id),
    CONSTRAINT fk_book_category_book
        FOREIGN KEY (book_id) REFERENCES book (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_book_category_category
        FOREIGN KEY (category_id) REFERENCES category (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS user_address (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    district VARCHAR(50) NULL,
    detail_address VARCHAR(255) NOT NULL,
    postal_code VARCHAR(10) NULL,
    default_address BOOLEAN NOT NULL DEFAULT FALSE,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_address_user (user_id),
    CONSTRAINT fk_address_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    selected BOOLEAN NOT NULL DEFAULT TRUE,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_cart_user_book UNIQUE (user_id, book_id),
    KEY idx_cart_user (user_id),
    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_cart_book
        FOREIGN KEY (book_id) REFERENCES book (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_cart_quantity CHECK (quantity BETWEEN 1 AND 999)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS book_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    status ENUM(
        'PENDING_PAYMENT',
        'PENDING_SHIPMENT',
        'SHIPPED',
        'COMPLETED',
        'CANCELLED'
    ) NOT NULL DEFAULT 'PENDING_PAYMENT',
    total_amount DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    shipping_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    payable_amount DECIMAL(10, 2) NOT NULL,
    expire_time DATETIME(6) NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    remark VARCHAR(255) NULL,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    paid_time DATETIME(6) NULL,
    shipped_time DATETIME(6) NULL,
    completed_time DATETIME(6) NULL,
    cancelled_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_order_no UNIQUE (order_no),
    KEY idx_order_user_create_time (user_id, create_time),
    KEY idx_order_status (status),
    KEY idx_order_status_expire_time (status, expire_time),
    CONSTRAINT fk_order_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_order_total_amount CHECK (total_amount >= 0),
    CONSTRAINT chk_order_discount_amount CHECK (discount_amount >= 0),
    CONSTRAINT chk_order_shipping_fee CHECK (shipping_fee >= 0),
    CONSTRAINT chk_order_payable_amount CHECK (payable_amount >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    book_title VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_order_item_order (order_id),
    KEY idx_order_item_book (book_id),
    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id) REFERENCES book_order (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_order_item_book
        FOREIGN KEY (book_id) REFERENCES book (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_order_item_price CHECK (unit_price >= 0),
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_item_subtotal CHECK (subtotal >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS payment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    payment_no VARCHAR(50) NOT NULL,
    order_id BIGINT NOT NULL,
    payment_method ENUM('ALIPAY', 'WECHAT_PAY', 'BANK_CARD', 'MOCK') NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'CLOSED', 'REFUNDED')
        NOT NULL DEFAULT 'PENDING',
    paid_time DATETIME(6) NULL,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_payment_no UNIQUE (payment_no),
    KEY idx_payment_order (order_id),
    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id) REFERENCES book_order (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_payment_amount CHECK (amount >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS book_review (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    rating INT NOT NULL,
    content VARCHAR(1000) NULL,
    status INT NOT NULL DEFAULT 1,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_review_order_item UNIQUE (order_item_id),
    KEY idx_review_book (book_id),
    CONSTRAINT fk_review_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_review_book
        FOREIGN KEY (book_id) REFERENCES book (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_review_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_item (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_review_status CHECK (status IN (0, 1))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS inventory_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    book_id BIGINT NOT NULL,
    change_quantity INT NOT NULL,
    before_stock INT NOT NULL,
    after_stock INT NOT NULL,
    change_type ENUM(
        'PURCHASE_IN',
        'ORDER_OUT',
        'ORDER_CANCEL_RETURN',
        'MANUAL_ADJUSTMENT'
    ) NOT NULL,
    order_id BIGINT NULL,
    remark VARCHAR(255) NULL,
    create_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_inventory_book (book_id),
    KEY idx_inventory_order (order_id),
    CONSTRAINT fk_inventory_book
        FOREIGN KEY (book_id) REFERENCES book (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_order
        FOREIGN KEY (order_id) REFERENCES book_order (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_inventory_before_stock CHECK (before_stock >= 0),
    CONSTRAINT chk_inventory_after_stock CHECK (after_stock >= 0)
) ENGINE = InnoDB;
