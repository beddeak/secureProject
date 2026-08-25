package com.securearchive.archive.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.securearchive.archive.document.exception.InvalidDocumentStateException;

class DocumentStateTests {
    @Test
    void draftDocumentCanBeSubmittedReviewedAndApproved() {
        Document document = documentWithStatus(DocumentStatus.DRAFT);

        document.submitForReview();
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.PENDING_REVIEW);

        document.review();
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.REVIEWED);

        document.approve();
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.PUBLISHED);
        assertThat(document.getPublishedAt()).isNotNull();
    }

    @Test
    void pendingDocumentCannotBeApprovedWithoutReview() {
        Document document = documentWithStatus(DocumentStatus.PENDING_REVIEW);

        assertThatThrownBy(document::approve)
            .isInstanceOf(InvalidDocumentStateException.class);
    }

    @Test
    void pendingDocumentCanBeRejected() {
        Document document = documentWithStatus(DocumentStatus.PENDING_REVIEW);

        document.reject();

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(document.getPublishedAt()).isNull();
    }

    @Test
    void reviewedDocumentCanBeRejected() {
        Document document = documentWithStatus(DocumentStatus.REVIEWED);

        document.reject();

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.REJECTED);
    }

    @Test
    void publishedDocumentReturnsToDraftWhenUpdated() {
        Document document = documentWithStatus(DocumentStatus.PUBLISHED);

        document.update("수정 문서", 0, "수정 요약", "수정 본문");

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(document.getPublishedAt()).isNull();
        assertThat(document.getTitle()).isEqualTo("수정 문서");
    }

    @Test
    void pendingDocumentCannotBeUpdated() {
        Document document = documentWithStatus(DocumentStatus.PENDING_REVIEW);

        assertThatThrownBy(() -> document.update("수정 문서", 0, null, "수정 본문"))
            .isInstanceOf(InvalidDocumentStateException.class);
    }

    @Test
    void reviewedDocumentCannotBeUpdated() {
        Document document = documentWithStatus(DocumentStatus.REVIEWED);

        assertThatThrownBy(() -> document.update("수정 문서", 0, null, "수정 본문"))
            .isInstanceOf(InvalidDocumentStateException.class);
    }

    private Document documentWithStatus(DocumentStatus status) {
        return Document.builder()
            .documentCode("DOC-TEST")
            .title("테스트 문서")
            .documentType(DocumentType.GENERAL_RECORD)
            .requiredClearanceLevel(0)
            .status(status)
            .content("테스트 본문")
            .build();
    }
}
