package com.securearchive.archive.document;

import com.securearchive.archive.document.exception.InvalidDocumentStateException;
import com.securearchive.archive.user.User;
import com.securearchive.archive.department.Department;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;


@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_code", nullable = false, unique = true, length = 50)
    private String documentCode;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "required_clearance_level", nullable = false)
    @Builder.Default
    private Integer requiredClearanceLevel = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void submitForReview() {
        if (status != DocumentStatus.DRAFT && status != DocumentStatus.REJECTED) {
            throw new InvalidDocumentStateException("초안 또는 반려 상태의 문서만 검토 요청할 수 있습니다");
        }

        status = DocumentStatus.PENDING_REVIEW;
    }

    public void review() {
        if (status != DocumentStatus.PENDING_REVIEW) {
            throw new InvalidDocumentStateException("검토 대기 상태의 문서만 검토 완료할 수 있습니다");
        }

        status = DocumentStatus.REVIEWED;
    }

    public void approve() {
        if (status != DocumentStatus.REVIEWED) {
            throw new InvalidDocumentStateException("검토 완료 상태의 문서만 최종 승인할 수 있습니다");
        }

        status = DocumentStatus.PUBLISHED;
        publishedAt = LocalDateTime.now();
    }

    public void reject() {
        if (status != DocumentStatus.PENDING_REVIEW && status != DocumentStatus.REVIEWED) {
            throw new InvalidDocumentStateException("검토 중인 문서만 반려할 수 있습니다");
        }

        status = DocumentStatus.REJECTED;
        publishedAt = null;
    }

    public void update(
        String title,
        Integer requiredClearanceLevel,
        String summary,
        String content
    ) {
        if (status == DocumentStatus.ARCHIVED) {
            throw new InvalidDocumentStateException("보관된 문서는 수정할 수 없습니다");
        }
        if (status == DocumentStatus.PENDING_REVIEW || status == DocumentStatus.REVIEWED) {
            throw new InvalidDocumentStateException("검토 중인 문서는 수정할 수 없습니다");
        }

        this.title = title;
        this.requiredClearanceLevel = requiredClearanceLevel;
        this.summary = summary;
        this.content = content;

        if (status == DocumentStatus.PUBLISHED || status == DocumentStatus.REJECTED) {
            status = DocumentStatus.DRAFT;
            publishedAt = null;
        }
    }
}
