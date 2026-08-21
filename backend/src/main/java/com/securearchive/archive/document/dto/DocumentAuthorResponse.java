package com.securearchive.archive.document.dto;

import com.securearchive.archive.user.User;

public record DocumentAuthorResponse(
    Long id,
    String nickname,
    String title
) {
    public static DocumentAuthorResponse from(User user) {
        return new DocumentAuthorResponse(user.getId(), user.getNickname(), user.getTitle());
    }
}
