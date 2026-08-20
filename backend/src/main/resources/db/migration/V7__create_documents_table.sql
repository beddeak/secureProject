CREATE TABLE documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    document_code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    document_type VARCHAR(50) NOT NULL,

    author_id BIGINT NOT NULL,
    department_id BIGINT NULL,

    required_clearance_level INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    summary TEXT NULL,
    content LONGTEXT NOT NULL,
    published_at DATETIME NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_documents_author
        FOREIGN KEY (author_id)
        REFERENCES users(id),

    CONSTRAINT fk_documents_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id),

    CONSTRAINT chk_documents_clearance
        CHECK (required_clearance_level BETWEEN 0 AND 10)
);