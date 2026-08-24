package com.securearchive.archive.document;

import com.securearchive.archive.document.dto.DocumentResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.securearchive.archive.document.dto.DocumentCreateRequest;
import com.securearchive.archive.security.AuthenticatedUser;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    public final DocumentService documentService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(
        @Valid @RequestBody DocumentCreateRequest request,
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return documentService.createDocument(request, user.id());
    }
    @GetMapping
    public List<DocumentResponse> getDocument(@AuthenticationPrincipal AuthenticatedUser user)
        {
            int clearanceLevel = user == null ? 0 : user.clearanceLevel();

            return documentService.getDocuments(clearanceLevel);
    }
    @PatchMapping("/{documentId}/publish")
    public DocumentResponse publishDocument(
        @PathVariable("documentId") Long documentId,
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return documentService.publishDocument(documentId, user.id());
    }
}
