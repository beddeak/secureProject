package com.securearchive.archive.membership;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securearchive.archive.membership.dto.DepartmentMembershipResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final UserDepartmentMembershipRepository membershipRepository;
    

    @Transactional(readOnly = true)
    public List<DepartmentMembershipResponse> getActiveMemberships(Long userId) {
        return membershipRepository.findByUser_IdAndLeftAtIsNull(userId)
            .stream()
            .map(DepartmentMembershipResponse::from)
            .toList();
    }
}
