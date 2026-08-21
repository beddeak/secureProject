package com.securearchive.archive.document;

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
    public List<DocumentResponse> getDocuments() {
        return documentRepository.findAll()
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
}
