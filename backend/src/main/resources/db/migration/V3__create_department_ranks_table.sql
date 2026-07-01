CREATE TABLE department_ranks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    department_id BIGINT NOT NULL,

    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    level_order INT NOT NULL,

    description TEXT,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_department_ranks_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id),
    CONSTRAINT uq_department_rank_code
        UNIQUE (department_id, code),
    CONSTRAINT uq_department_rank_name
        UNIQUE (department_id, name)
);
