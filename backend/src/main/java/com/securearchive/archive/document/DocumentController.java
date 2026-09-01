package com.securearchive.archive.document;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.securearchive.archive.document.dto.DocumentCreateRequest;
import com.securearchive.archive.document.dto.DocumentResponse;
import com.securearchive.archive.document.dto.DocumentUpdateRequest;
import com.securearchive.archive.security.AuthenticatedUser;
import com.securearchive.archive.user.UserRole;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @GetMapping
    public List<DocumentResponse> getDocuments(
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long requesterId = user == null ? null : user.id();
        UserRole requesterRole = user == null ? null : user.role();
        int clearanceLevel = user == null ? null : user.clearanceLevel();

        return documentService.getDocuments(requesterId, requesterRole, clearanceLevel);
    }

    @GetMapping("/{documentId}")
    public DocumentResponse getDocument(
        @PathVariable Long documentId,
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long requesterId = user == null ? null : user.id();
        UserRole requesterRole = user == null ? null : user.role();
        int clearanceLevel = user == null ? 0 : user.clearanceLevel();

        return documentService.getDocument(
            documentId,
            requesterId,
            requesterRole,
            clearanceLevel
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(
        @Valid @RequestBody DocumentCreateRequest request,
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return documentService.createDocument(request, user.id());
    }

    @PutMapping("/{documentId}")
    public DocumentResponse updateDocument(
        @PathVariable Long documentId,
        @Valid @RequestBody DocumentUpdateRequest request,
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return documentService.updateDocument(
            documentId,
            user.id(),
            user.role(),
            user.clearanceLevel(),
            request
        );
    }

    @PatchMapping("/{documentId}/submit")
    public DocumentResponse submitForReview(
        @PathVariable Long documentId,
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return documentService.submitForReview(documentId, user.id());
    }

    @PatchMapping("/{documentId}/approve")
    public DocumentResponse approveDocument(
        @PathVariable Long documentId,
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return documentService.approveDocument(documentId, user.id(), user.role());
    }

    @PatchMapping("/{documentId}/reject")
    public DocumentResponse rejectDocument(
        @PathVariable Long documentId,
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return documentService.rejectDocument(documentId, user.id(), user.role());
    }
}
