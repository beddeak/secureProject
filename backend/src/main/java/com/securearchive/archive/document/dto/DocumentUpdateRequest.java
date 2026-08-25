package com.securearchive.archive.document.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentUpdateRequest(
    @NotBlank
    @Size(max = 200)
    String title,

    @NotNull
    @Min(0)
    @Max(10)
    Integer requiredClearanceLevel,

    String summary,

    @NotBlank
    String content

) {
}
