package com.securearchive.archive.document;

import java.util.List;

import com.securearchive.archive.document.dto.DocumentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments() {
        return documentRepository.findAll()
            .stream()
            .map(DocumentResponse::from)
            .toList();
    }
}
