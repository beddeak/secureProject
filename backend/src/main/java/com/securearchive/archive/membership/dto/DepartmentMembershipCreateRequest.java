package com.securearchive.archive.membership.dto;

import jakarta.validation.constraints.NotNull;

public record DepartmentMembershipCreateRequest(
    @NotNull Long userId,
    @NotNull Long departmentRankId
) {
    
} 