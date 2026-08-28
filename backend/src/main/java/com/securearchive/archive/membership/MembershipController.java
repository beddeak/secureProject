package com.securearchive.archive.membership;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securearchive.archive.membership.dto.DepartmentMembershipResponse;
import com.securearchive.archive.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/users/me/memberships")
@RequiredArgsConstructor
public class MembershipController {
    private final MembershipService membershipService;
    
    @GetMapping()
    public List<DepartmentMembershipResponse> getMyMemberships(@AuthenticationPrincipal AuthenticatedUser user) {
        return membershipService.getActiveMemberships(user.id());
    }
}
