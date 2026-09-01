package com.securearchive.archive.document;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface DocumentRepository extends JpaRepository<Document, Long>{
    Optional<Document> findByDocumentCode(String documentCode);

    boolean existsByDocumentCode(String document);

    List<Document> findAllByOrderByCreatedAtDesc();
}