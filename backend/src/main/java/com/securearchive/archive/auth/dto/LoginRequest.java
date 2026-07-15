package com.securearchive.archive.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @Email(message = "올바른 이메일이 아닙니다")
    @NotBlank(message = "이메일은 필수입니다")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message =  "비밀번호는 최소 8자리입니다")
    String password
) {
}