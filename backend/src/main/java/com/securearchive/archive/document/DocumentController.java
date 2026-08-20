package com.securearchive.archive.document;

import com.securearchive.archive.document.dto.DocumentResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
