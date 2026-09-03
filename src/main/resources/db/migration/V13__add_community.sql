CREATE TABLE community_post (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    content TEXT NOT NULL,
    status INT NOT NULL DEFAULT 1,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_community_post_create_time (create_time, id),
    KEY idx_community_post_user_create_time (user_id, create_time),
    CONSTRAINT fk_community_post_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_community_post_status CHECK (status IN (0, 1))
) ENGINE = InnoDB;

CREATE TABLE community_post_image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    create_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_community_post_image_post (post_id, sort_order, id),
    CONSTRAINT fk_community_post_image_post FOREIGN KEY (post_id) REFERENCES community_post (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_community_post_image_sort_order CHECK (sort_order >= 0)
) ENGINE = InnoDB;

CREATE TABLE community_post_book (
    post_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, book_id),
    KEY idx_community_post_book_book (book_id, post_id),
    CONSTRAINT fk_community_post_book_post FOREIGN KEY (post_id) REFERENCES community_post (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_community_post_book_book FOREIGN KEY (book_id) REFERENCES book (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE community_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    content VARCHAR(1000) NOT NULL,
    status INT NOT NULL DEFAULT 1,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_community_comment_post_create (post_id, create_time, id),
    KEY idx_community_comment_parent (parent_id),
    CONSTRAINT fk_community_comment_post FOREIGN KEY (post_id) REFERENCES community_post (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_community_comment_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_community_comment_parent FOREIGN KEY (parent_id) REFERENCES community_comment (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_community_comment_status CHECK (status IN (0, 1))
) ENGINE = InnoDB;
