package com.securearchive.archive.membership;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securearchive.archive.membership.dto.DepartmentMembershipResponse;
import com.securearchive.archive.auth.exception.DuplicateResourceException;
import com.securearchive.archive.common.error.ResourceNotFoundException;
import com.securearchive.archive.department.Department;
import com.securearchive.archive.department.DepartmentRank;
import com.securearchive.archive.department.DepartmentRankRepository;
import com.securearchive.archive.department.DepartmentRepository;
import com.securearchive.archive.membership.dto.DepartmentMemberResponse;
import com.securearchive.archive.membership.dto.DepartmentMembershipCreateRequest;
import com.securearchive.archive.user.*;

import org.springframework.security.access.AccessDeniedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private static final int MEMBER_VIEW_LEVEL = 4; //목록 조회
    private static final int MEMBER_MANAGE_LEVEL = 4; //직원 추가
    private final UserDepartmentMembershipRepository membershipRepository;
    private final DepartmentPermissionService departmentPermissionService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentRankRepository departmentRankRepository;

    

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
        boolean hasDepartmentAuthority = membershipRepository.countActiveMembershipsWithMinimumRank(requesterId, departmentId, MEMBER_MANAGE_LEVEL) > 0;

        if (!hasGlobalAuthority && !hasDepartmentAuthority) {
            throw new AccessDeniedException("같은 부서의 Level-5 이상 또는 전역 관리자만 조회할 수 있습니다");

            }
            return membershipRepository.findByDepartment_IdAndLeftAtIsNull(departmentId)
                .stream()
                .map(DepartmentMemberResponse::from)
                .toList();
    }
    @Transactional
    public DepartmentMemberResponse addDepartmentMember(
        Long requesterId, 
        UserRole requesterRole, 
        Long departmentId,
        DepartmentMembershipCreateRequest request
    ) {
        boolean hasGlobalAuthority = hasGlobalMemberAuthority(requesterRole, requesterId);
        boolean hasDepartmentAuthority = membershipRepository.countActiveMembershipsWithMinimumRank(requesterId, departmentId, MEMBER_VIEW_LEVEL) > 0;

        if (!hasGlobalAuthority && !hasDepartmentAuthority) {
            throw new AccessDeniedException("같은 부서의 Level-4 이상 또는 전역 관리자만 조회할 수 있습니다");
        }
            User targetUser = userRepository.findById(request.userId())
                .orElseThrow(() ->
                    new ResourceNotFoundException("사용자를 찾을 수 없습니다")
                );

            Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("부서를 찾을 수 없습니다")
                );

            DepartmentRank departmentRank =
                departmentRankRepository.findById(request.departmentRankId())
                    .orElseThrow(() ->
                        new ResourceNotFoundException("부서 계급을 찾을 수 없습니다")
        );
        if (!departmentRank.getDepartment().getId().equals(departmentId)) {
            throw new ResourceNotFoundException("해당 부서의 계급이 아닙니다");
        }
        if (membershipRepository.existsByUser_IdAndDepartment_IdAndLeftAtIsNull(request.userId(), departmentId)) {
            throw new DuplicateResourceException("이미 해당 부서에 소속된 사용자입니다");
        }
        if (!hasGlobalAuthority) {
            UserDepartmentMembership requestMembership = membershipRepository.findByUser_IdAndDepartment_IdAndLeftAtIsNull(
                requesterId,
                departmentId
                     ).orElseThrow(() -> new AccessDeniedException("해당 부서 소속이 아닙니다"));
                     int requesterLevel = requestMembership.getDepartmentRank().getLevelOrder();
                    
                     if (departmentRank.getLevelOrder() >= requesterLevel) {
                        throw new AccessDeniedException("자신과 같거나 높은 계급은 배정할 수 없습니다");
                     }
        }

        UserDepartmentMembership newMembership = UserDepartmentMembership.builder()
            .user(targetUser)
            .department(department)
            .departmentRank(departmentRank)
            .build();

        UserDepartmentMembership saved = membershipRepository.save(newMembership);

        return DepartmentMemberResponse.from(saved);
    }

    private boolean hasGlobalMemberAuthority(UserRole role, Long userId) {
        if (role == UserRole.AION_COUNCIL) {
            return departmentPermissionService.isActiveOverwatchCommander(userId);
        }
        return role == UserRole.VICE_ADMINISTRATOR || role == UserRole.ADMINISTRATOR;
    }
}
