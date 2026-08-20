package com.securearchive.archive.document.dto;

import com.securearchive.archive.document.DocumentType;
import com.securearchive.archive.department.dto.DepartmentResponse;
import com.securearchive.archive.document.Document;
import com.securearchive.archive.document.DocumentStatus;
import com.securearchive.archive.user.dto.UserResponse;

import java.time.LocalDateTime;

public record DocumentResponse(
    Long id,
    String documentCode,
    String title,
    DocumentType documentType,
    UserResponse author,
    DepartmentResponse department,
    Integer requiredClearanceLevel,
    DocumentStatus status,
    String summary,
    String content,
    LocalDateTime publishedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
            document.getId(),
            document.getDocumentCode(),
            document.getTitle(),
            document.getDocumentType(),
            UserResponse.from(document.getAuthor()),
            document.getDepartment() == null
                ? null
                : DepartmentResponse.from(document.getDepartment()),
            document.getRequiredClearanceLevel(),
            document.getStatus(),
            document.getSummary(),
            document.getContent(),
            document.getPublishedAt(),
            document.getCreatedAt(),
            document.getUpdatedAt()
        );
    }
}
