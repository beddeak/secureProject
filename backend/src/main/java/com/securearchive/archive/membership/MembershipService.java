package com.securearchive.archive.membership;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securearchive.archive.membership.dto.DepartmentMembershipResponse;
import com.securearchive.archive.user.UserRole;
import com.securearchive.archive.membership.dto.DepartmentMemberResponse;
import org.springframework.security.access.AccessDeniedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private static final int MEMBER_VIEW_LEVEL = 5;
    private final UserDepartmentMembershipRepository membershipRepository;
    private final DepartmentPermissionService departmentPermissionService;
    

    @Transactional(readOnly = true)
    public List<DepartmentMembershipResponse> getActiveMemberships(Long userId) {
        return membershipRepository.findByUser_IdAndLeftAtIsNull(userId)
            .stream()
            .map(DepartmentMembershipResponse::from)
            .toList();
    }
    @Transactional(readOnly = true)
    public List<DepartmentMemberResponse> getActiveDepartmentMembers(
        Long requesterId,
        UserRole requesterUserRole,
        Long departmentId
    ) {
        boolean hasGlobalAuthority = hasGlobalMemberAuthority(requesterUserRole, requesterId);
        boolean hasDepartmentAuthority = membershipRepository.countActiveMembershipsWithMinimumRank(requesterId, departmentId, MEMBER_VIEW_LEVEL) > 0;

        if (!hasGlobalAuthority && !hasDepartmentAuthority) {
            throw new AccessDeniedException("같은 부서의 Level-5 이상 또는 전역 관리자만 조회할 수 있습니다");

            }
            return membershipRepository.findByDepartment_IdAndLeftAtIsNull(departmentId)
                .stream()
                .map(DepartmentMemberResponse::from)
                .toList();
    }
    private boolean hasGlobalMemberAuthority(UserRole role, Long userId) {
        if (role == UserRole.AION_COUNCIL) {
            return departmentPermissionService.isActiveOverwatchCommander(userId);
        }
        return role == UserRole.SITE_DIRECTOR || role == UserRole.VICE_ADMINISTRATOR || role == UserRole.ADMINISTRATOR;
    }
}
