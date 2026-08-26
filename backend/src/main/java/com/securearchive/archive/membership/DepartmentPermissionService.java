package com.securearchive.archive.membership;


import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentPermissionService {
    private static final String OVERWATCH_COMMANDER_CODE = "SENIOR_OVERWATCH_COMMANDER";

    private final UserDepartmentMembershipRepository membershipRepository;


    @Transactional(readOnly = true)
    public boolean isActiveOverwatchCommander(Long userId) {
        return membershipRepository.existsByUser_IdAndLeftAtIsNullAndDepartmentRank_Code(userId, OVERWATCH_COMMANDER_CODE);
    }
}
