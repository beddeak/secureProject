package com.securearchive.archive.document;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;

import com.securearchive.archive.auth.exception.DuplicateResourceException;
import com.securearchive.archive.department.Department;
import com.securearchive.archive.department.DepartmentRepository;
import com.securearchive.archive.document.dto.DocumentCreateRequest;
import com.securearchive.archive.document.dto.DocumentResponse;
import com.securearchive.archive.user.UserRepository;
import com.securearchive.archive.user.User;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(Integer clearanceLevel) {
        return documentRepository
            .findByStatusAndRequiredClearanceLevelLessThanEqualOrderByCreatedAtDesc(
                DocumentStatus.PUBLISHED,
                clearanceLevel
            )
            .stream()
            .map(DocumentResponse::from)
            .toList();
    }
    @Transactional
    public DocumentResponse createDocument(
        DocumentCreateRequest request,
        Long authorId
    )  {
        if (documentRepository.existsByDocumentCode(request.documentCode())) {
            throw new DuplicateResourceException("이미 존재하는 문서 코드입니다");
        }
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Department department = null;

        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다"));
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
            Document savedDocument  = documentRepository.save(document);

            return DocumentResponse.from(savedDocument);


    }
    @Transactional
    public DocumentResponse publishDocument(Long documentId, Long requesterId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 가 없습니다"));

        if (!document.getAuthor().getId().equals(requesterId)) {
            throw new AccessDeniedException("작성자만 문서를 공개할 수 있습니다");
        }

        document.publish();

        return DocumentResponse.from(document);
    }
    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long documentId, Long requesterId, Integer clearanceLevel) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다"));

        boolean isAuthor = requesterId != null && document.getAuthor().getId().equals(requesterId);

        boolean canReadPublished = document.getStatus() == DocumentStatus.PUBLISHED && document.getRequiredClearanceLevel() <= clearanceLevel;
        if(!isAuthor && !canReadPublished) {
            throw new AccessDeniedException("문서를 열람할 권한이 없습니다");
        }

        return DocumentResponse.from(document);
    }
}
