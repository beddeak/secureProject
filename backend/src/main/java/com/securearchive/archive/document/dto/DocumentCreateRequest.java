package com.securearchive.archive.document.dto;

import com.securearchive.archive.document.DocumentType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentCreateRequest(
    @NotBlank
    @Size(max = 50)
    String documentCode,

    @NotBlank
    @Size(max = 50)
    String title,

    @NotNull
    DocumentType documentType,

    Long departmentId,

    @NotNull
    @Min(0)
    @Max(10)
    Integer requiredClearanceLevel,

    String summary,

    @NotBlank
    String content
) {
} 
