package com.securearchive.archive.membership.dto;

import java.time.LocalDateTime;

import com.securearchive.archive.department.dto.DepartmentRankResponse;
import com.securearchive.archive.membership.UserDepartmentMembership;


public record DepartmentMemberResponse(
    Long membershipId,
    Long userId,
    String nickname,
    String title,
    DepartmentRankResponse rank,
    LocalDateTime joinedAt,
    Integer clearanceLevel
) {
    public static DepartmentMemberResponse from(UserDepartmentMembership membership) {
        return new DepartmentMemberResponse(membership.getId(), membership.getUser().getId(), membership.getUser().getNickname(), membership.getUser().getTitle(), DepartmentRankResponse.from(membership.getDepartmentRank()),membership.getJoinedAt(),membership.getUser().getClearanceLevel());
    }
} 
