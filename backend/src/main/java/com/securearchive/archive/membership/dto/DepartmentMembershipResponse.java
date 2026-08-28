package com.securearchive.archive.membership.dto;

import java.time.LocalDateTime;

import com.securearchive.archive.department.dto.DepartmentRankResponse;
import com.securearchive.archive.department.dto.DepartmentResponse;
import com.securearchive.archive.membership.UserDepartmentMembership;


public record DepartmentMembershipResponse(
    Long id,
    DepartmentResponse department,
    DepartmentRankResponse rank,
    LocalDateTime joinedAt
) {
    
    public static DepartmentMembershipResponse from(UserDepartmentMembership membership) {
        return new DepartmentMembershipResponse(
            membership.getId(), 
            DepartmentResponse.from(membership.getDepartment()),
            DepartmentRankResponse.from(membership.getDepartmentRank()),
            membership.getJoinedAt()
        );
    }
} 