CREATE TABLE department_memberships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    department_rank_id BIGINT NULL,

    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_memberships_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT fk_memberships_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id),
    CONSTRAINT fk_memberships_rank
        FOREIGN KEY (department_rank_id)
        REFERENCES department_ranks(id)
);
