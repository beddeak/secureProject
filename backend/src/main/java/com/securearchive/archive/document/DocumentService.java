package com.securearchive.archive.document;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securearchive.archive.membership.DepartmentPermissionService;
import com.securearchive.archive.auth.exception.DuplicateResourceException;
import com.securearchive.archive.common.error.ResourceNotFoundException;
import com.securearchive.archive.department.Department;
import com.securearchive.archive.department.DepartmentRepository;
import com.securearchive.archive.document.dto.DocumentCreateRequest;
import com.securearchive.archive.document.dto.DocumentResponse;
import com.securearchive.archive.document.dto.DocumentUpdateRequest;
import com.securearchive.archive.membership.UserDepartmentMembershipRepository;
import com.securearchive.archive.user.User;
import com.securearchive.archive.user.UserRepository;
import com.securearchive.archive.user.UserRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private static final int REVIEWER_LEVEL = 4;
    private static final int APPROVAL_LEVEL = 5;

    private final DepartmentPermissionService departmentPermissionService;
    private final DocumentRepository documentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final UserDepartmentMembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(Long requesterId, UserRole requesterRole ,Integer clearanceLevel) {
        boolean hasGlobalAuthority = hasGlobalDocumentAuthority(requesterRole, requesterId);
        return documentRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .filter(document -> {
                boolean isPublishedAndAccessible = document.getStatus() == DocumentStatus.PUBLISHED && document.getRequiredClearanceLevel() <= clearanceLevel;
                boolean isAuthor = requesterId != null && document.getAuthor().getId().equals(requesterId);
                boolean canReview = document.getStatus() == DocumentStatus.PENDING_REVIEW && hasDepartmentAuthority(requesterId, document, REVIEWER_LEVEL);

                return isPublishedAndAccessible || isAuthor || hasGlobalAuthority || canReview;
            })
            .map(DocumentResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(
        Long documentId,
        Long requesterId,
        UserRole requesterRole,
        Integer clearanceLevel
    ) {
        Document document = findDocument(documentId);

        boolean isAuthor = requesterId != null
            && document.getAuthor().getId().equals(requesterId);
        boolean hasGlobalAuthority = hasGlobalDocumentAuthority(requesterRole, requesterId);
        boolean isUnderReview = document.getStatus() == DocumentStatus.PENDING_REVIEW;
        boolean hasDepartmentReviewAuthority = isUnderReview
            && hasDepartmentAuthority(requesterId, document, REVIEWER_LEVEL);
        boolean canReadPublished = document.getStatus() == DocumentStatus.PUBLISHED
            && document.getRequiredClearanceLevel() <= clearanceLevel;

        if (!isAuthor
            && !hasGlobalAuthority
            && !hasDepartmentReviewAuthority
            && !canReadPublished) {
            throw new AccessDeniedException("문서를 열람할 권한이 없습니다");
        }

        return DocumentResponse.from(document);
    }

    @Transactional
    public DocumentResponse createDocument(DocumentCreateRequest request, Long authorId) {
        if (documentRepository.existsByDocumentCode(request.documentCode())) {
            throw new DuplicateResourceException("이미 존재하는 문서 코드입니다");
        }

        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));

        if (request.requiredClearanceLevel() > author.getClearanceLevel()) {
            throw new AccessDeniedException("자신의 등급보다 높은 문서를 작성할 수 없습니다");
        }

        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("부서를 찾을 수 없습니다"));

            boolean isActiveMember = membershipRepository
                .existsByUser_IdAndDepartment_IdAndLeftAtIsNull(
                    authorId,
                    request.departmentId()
                );
            if (!isActiveMember) {
                throw new AccessDeniedException("소속된 부서만 문서의 담당 부서로 지정할 수 있습니다");
            }
        }

        Document document = Document.builder()
            .documentCode(request.documentCode())
            .title(request.title())
            .documentType(request.documentType())
            .author(author)
            .department(department)
            .requiredClearanceLevel(request.requiredClearanceLevel())
            .status(DocumentStatus.DRAFT)
            .summary(request.summary())
            .content(request.content())
            .build();

        return DocumentResponse.from(documentRepository.save(document));
    }

    @Transactional
    public DocumentResponse updateDocument(
        Long documentId,
        Long requesterId,
        UserRole requesterRole,
        Integer requesterClearanceLevel,
        DocumentUpdateRequest request
    ) {
        Document document = findDocument(documentId);

        boolean isAuthor = document.getAuthor().getId().equals(requesterId);
        boolean hasGlobalAuthority = hasGlobalDocumentAuthority(requesterRole, requesterId);
        if (!isAuthor && !hasGlobalAuthority) {
            throw new AccessDeniedException("문서를 수정할 권한이 없습니다");
        }
        if (!hasGlobalAuthority && request.requiredClearanceLevel() > requesterClearanceLevel) {
            throw new AccessDeniedException("자신의 등급보다 높은 문서로 변경할 수 없습니다");
        }

        document.update(
            request.title(),
            request.requiredClearanceLevel(),
            request.summary(),
            request.content()
        );

        return DocumentResponse.from(document);
    }

    @Transactional
    public DocumentResponse submitForReview(Long documentId, Long requesterId) {
        Document document = findDocument(documentId);

        if (!document.getAuthor().getId().equals(requesterId)) {
            throw new AccessDeniedException("작성자만 문서를 검토 요청할 수 있습니다");
        }

        document.submitForReview();
        return DocumentResponse.from(document);
    }

    @Transactional
    public DocumentResponse approveDocument(
        Long documentId,
        Long approverId,
        UserRole approverRole
    ) {
        Document document = findDocument(documentId);
        requireDepartmentAuthority(document, approverId, approverRole, APPROVAL_LEVEL);

        document.approve();
        return DocumentResponse.from(document);
    }

    @Transactional
    public DocumentResponse rejectDocument(
        Long documentId,
        Long reviewerId,
        UserRole reviewerRole
    ) {
        Document document = findDocument(documentId);
        requireDepartmentAuthority(document, reviewerId, reviewerRole, REVIEWER_LEVEL);

        document.reject();
        return DocumentResponse.from(document);
    }

    private Document findDocument(Long documentId) {
        return documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("문서를 찾을 수 없습니다"));
    }

    private void requireDepartmentAuthority(
        Document document,
        Long userId,
        UserRole role,
        int minimumLevel
    ) {
        if (hasGlobalDocumentAuthority(role, userId)) {
            return;
        }

        if (!hasDepartmentAuthority(userId, document, minimumLevel)) {
            String message = minimumLevel == APPROVAL_LEVEL
                ? "같은 부서의 Level-5 이상만 문서를 최종 승인할 수 있습니다"
                : "같은 부서의 Level-4 이상만 문서를 반려할 수 있습니다";
            throw new AccessDeniedException(message);
        }
    }

    private boolean hasDepartmentAuthority(
        Long userId,
        Document document,
        int minimumLevel
    ) {
        if (userId == null || document.getDepartment() == null) {
            return false;
        }

        return membershipRepository.countActiveMembershipsWithMinimumRank(
            userId,
            document.getDepartment().getId(),
            minimumLevel
        ) > 0;
    }

    private boolean hasGlobalDocumentAuthority(UserRole role, Long userId) {
        if (role == UserRole.AION_COUNCIL) {
            return departmentPermissionService.isActiveOverwatchCommander(userId);
        }
        return role == UserRole.SITE_DIRECTOR || role == UserRole.VICE_ADMINISTRATOR || role == UserRole.ADMINISTRATOR;
    }
}
