package com.securearchive.archive.department;

import com.securearchive.archive.department.dto.DepartmentRankResponse;
import com.securearchive.archive.department.dto.DepartmentResponse;
import com.securearchive.archive.membership.MembershipService;
import com.securearchive.archive.membership.dto.DepartmentMemberResponse;
import com.securearchive.archive.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor

public class DepartmentController {

    public final MembershipService membershipService;
    public final DepartmentService departmentService;

    @GetMapping
    public List<DepartmentResponse> getDepartment() {
        return departmentService.getDepartments();
    }

    @GetMapping("/{departmentId}/ranks")
    public List<DepartmentRankResponse> getDepartmentRanks(@PathVariable Long departmentId) {
        return departmentService.getDepartmentRanks(departmentId);
    }
    @GetMapping("/{departmentId}/members")
    public List<DepartmentMemberResponse> getActiveMembers(@PathVariable Long departmentId, @AuthenticationPrincipal AuthenticatedUser user) {
        return membershipService.getActiveDepartmentMembers(
        user.id(),
        user.role(),
        departmentId
        );
    }
}