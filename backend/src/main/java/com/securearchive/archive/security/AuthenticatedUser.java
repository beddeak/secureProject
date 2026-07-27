package com.securearchive.archive.security;

import com.securearchive.archive.user.UserRole;

public record AuthenticatedUser(
    Long id,
    String email,
    UserRole role,
    Integer clearanceLevel
) {
}