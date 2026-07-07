package com.securearchive.archive.department.dto;

import com.securearchive.archive.department.DepartmentRank;

public record DepartmentRankResponse(
    Long id,
    String code,
    String name,
    Integer LevelOrder,
    String description
) {
    public static DepartmentRankResponse from(DepartmentRank rank) {
        return new DepartmentRankResponse(
            rank.getId(),
            rank.getCode(),
            rank.getName(),
            rank.getLevelOrder(),
            rank.getDescription()
        );
    }
}