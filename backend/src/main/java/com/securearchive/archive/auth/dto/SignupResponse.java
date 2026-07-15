package com.securearchive.archive.auth.dto;

import com.securearchive.archive.user.User;
import com.securearchive.archive.user.ClearanceLevels;
import com.securearchive.archive.user.UserRole;
import com.securearchive.archive.user.UserStatus;

public record SignupResponse(
    Long id,
    String email,
    String nickname,
    Integer ClearanceLevel,
    String ClearanceLevelName,
    UserRole role,
    String title,
    UserStatus status
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getClearanceLevel(),
            ClearanceLevels.toDisplayName(user.getClearanceLevel()),
            user.getRole(),
            user.getTitle(),
            user.getStatus()
        );
    }
}