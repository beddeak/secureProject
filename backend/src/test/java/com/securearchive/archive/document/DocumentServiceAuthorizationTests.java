package com.securearchive.archive.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.securearchive.archive.department.Department;
import com.securearchive.archive.department.DepartmentRepository;
import com.securearchive.archive.document.dto.DocumentCreateRequest;
import com.securearchive.archive.membership.UserDepartmentMembershipRepository;
import com.securearchive.archive.user.User;
import com.securearchive.archive.user.UserRepository;
import com.securearchive.archive.user.UserRole;

@ExtendWith(MockitoExtension.class)
class DocumentServiceAuthorizationTests {
    private static final Long DOCUMENT_ID = 1L;
    private static final Long REVIEWER_ID = 2L;
    private static final Long DEPARTMENT_ID = 10L;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDepartmentMembershipRepository membershipRepository;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void userWithoutDepartmentCanCreateUnassignedDocument() {
        User author = author(REVIEWER_ID);
        DocumentCreateRequest request = createRequest(null);
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(author));
        when(documentRepository.save(any(Document.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = documentService.createDocument(request, REVIEWER_ID);

        assertThat(response.department()).isNull();
        assertThat(response.status()).isEqualTo(DocumentStatus.DRAFT);
    }

    @Test
    void userCannotAssignDocumentToDepartmentTheyDoNotBelongTo() {
        User author = author(REVIEWER_ID);
        Department department = department();
        DocumentCreateRequest request = createRequest(DEPARTMENT_ID);
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(author));
        when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));
        when(membershipRepository.existsByUser_IdAndDepartment_IdAndLeftAtIsNull(
            REVIEWER_ID,
            DEPARTMENT_ID
        )).thenReturn(false);

        assertThatThrownBy(() -> documentService.createDocument(request, REVIEWER_ID))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void activeDepartmentMemberCanCreateDepartmentDocument() {
        User author = author(REVIEWER_ID);
        Department department = department();
        DocumentCreateRequest request = createRequest(DEPARTMENT_ID);
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(author));
        when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));
        when(membershipRepository.existsByUser_IdAndDepartment_IdAndLeftAtIsNull(
            REVIEWER_ID,
            DEPARTMENT_ID
        )).thenReturn(true);
        when(documentRepository.save(any(Document.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = documentService.createDocument(request, REVIEWER_ID);

        assertThat(response.department()).isNotNull();
        assertThat(response.department().id()).isEqualTo(DEPARTMENT_ID);
    }

    @Test
    void levelFourCanReviewPendingDocument() {
        Document document = documentWithStatus(DocumentStatus.PENDING_REVIEW);
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));
        when(membershipRepository.countActiveMembershipsWithMinimumRank(
            REVIEWER_ID,
            DEPARTMENT_ID,
            4
        )).thenReturn(1L);

        var response = documentService.reviewDocument(
            DOCUMENT_ID,
            REVIEWER_ID,
            UserRole.USER
        );

        assertThat(response.status()).isEqualTo(DocumentStatus.REVIEWED);
    }

    @Test
    void levelFourCannotGiveFinalApproval() {
        Document document = documentWithStatus(DocumentStatus.REVIEWED);
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));
        when(membershipRepository.countActiveMembershipsWithMinimumRank(
            REVIEWER_ID,
            DEPARTMENT_ID,
            5
        )).thenReturn(0L);

        assertThatThrownBy(() -> documentService.approveDocument(
            DOCUMENT_ID,
            REVIEWER_ID,
            UserRole.USER
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void levelFiveCanGiveFinalApproval() {
        Document document = documentWithStatus(DocumentStatus.REVIEWED);
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));
        when(membershipRepository.countActiveMembershipsWithMinimumRank(
            REVIEWER_ID,
            DEPARTMENT_ID,
            5
        )).thenReturn(1L);

        var response = documentService.approveDocument(
            DOCUMENT_ID,
            REVIEWER_ID,
            UserRole.USER
        );

        assertThat(response.status()).isEqualTo(DocumentStatus.PUBLISHED);
        assertThat(response.publishedAt()).isNotNull();
    }

    @Test
    void levelFourCanRejectDocumentUnderReview() {
        Document document = documentWithStatus(DocumentStatus.PENDING_REVIEW);
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));
        when(membershipRepository.countActiveMembershipsWithMinimumRank(
            REVIEWER_ID,
            DEPARTMENT_ID,
            4
        )).thenReturn(1L);

        var response = documentService.rejectDocument(
            DOCUMENT_ID,
            REVIEWER_ID,
            UserRole.USER
        );

        assertThat(response.status()).isEqualTo(DocumentStatus.REJECTED);
    }

    private Document documentWithStatus(DocumentStatus status) {
        return Document.builder()
            .id(DOCUMENT_ID)
            .documentCode("DOC-TEST")
            .title("테스트 문서")
            .documentType(DocumentType.GENERAL_RECORD)
            .author(author(100L))
            .department(department())
            .requiredClearanceLevel(0)
            .status(status)
            .content("테스트 본문")
            .build();
    }

    private User author(Long id) {
        return User.builder()
            .id(id)
            .email("author@test.local")
            .passwordHash("hash")
            .nickname("작성자")
            .build();
    }

    private Department department() {
        return Department.builder()
            .id(DEPARTMENT_ID)
            .code("TEST")
            .name("테스트 부서")
            .build();
    }

    private DocumentCreateRequest createRequest(Long departmentId) {
        return new DocumentCreateRequest(
            "DOC-NEW",
            "새 문서",
            DocumentType.GENERAL_RECORD,
            departmentId,
            0,
            null,
            "새 문서 본문"
        );
    }
}
