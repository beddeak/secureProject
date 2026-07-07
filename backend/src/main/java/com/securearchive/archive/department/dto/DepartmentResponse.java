package com.securearchive.archive.department.dto;

import com.securearchive.archive.department.Department;

public record DepartmentResponse(
    Long id,
    String code,
    String name,
    String description
) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
            department.getId(),
            department.getCode(),
            department.getName(),
            department.getDescription()
        );
    }
}