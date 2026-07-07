package com.securearchive.archive.user.dto;

import com.securearchive.archive.user.ClearanceLevels;
import com.securearchive.archive.user.User;

public record UserResponse(
    Long id,
    String email,
    String nickname,
    Integer activityLevel,
    Integer clearanceLevel,
    String clearanceName,
    String role,
    String title,
    String status
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getActivityLevel(),
            user.getClearanceLevel(),
            ClearanceLevels.toDisplayName(user.getClearanceLevel()),
            user.getRole().name(),
            user.getTitle(),
            user.getStatus().name()
        );
    
        }
}