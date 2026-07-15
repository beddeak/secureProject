package com.securearchive.archive.auth.dto;

import com.securearchive.archive.user.User;

public record LoginResponse(
    String accessTokne,
    String tokenType,
    SignupResponse user
) {
    public static LoginResponse of(String accessToken, User user) {
        return new LoginResponse(accessToken,"Bearer", SignupResponse.from(user)
        );
    }
}

