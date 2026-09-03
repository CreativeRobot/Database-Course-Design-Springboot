CREATE TABLE user_security_question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_key VARCHAR(30) NOT NULL,
    answer_hash VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_security_question UNIQUE (user_id, question_key),
    CONSTRAINT fk_user_security_question_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB;
