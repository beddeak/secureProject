package com.securearchive.archive.document;

import com.securearchive.archive.document.dto.DocumentResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.securearchive.archive.document.dto.DocumentCreateRequest;
import com.securearchive.archive.security.AuthenticatedUser;

import jakarta.validation.Valid;

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

    @GetMapping
    public List<DocumentResponse> getDocuments() {
        return documentService.getDocuments();
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(
        @Valid @RequestBody DocumentCreateRequest request,
        @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return documentService.createDocument(request, user.id());
    }
}
