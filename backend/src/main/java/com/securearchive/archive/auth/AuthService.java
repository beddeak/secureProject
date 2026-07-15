package com.securearchive.archive.auth;

import com.securearchive.archive.auth.dto.LoginRequest;
import com.securearchive.archive.auth.dto.LoginResponse;
import com.securearchive.archive.auth.dto.SignupRequest;
import com.securearchive.archive.auth.jwt.JwtTokenProvider;
import com.securearchive.archive.user.User;
import com.securearchive.archive.user.UserRepository;
import com.securearchive.archive.user.dto.UserResponse;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
        public final UserRepository userRepository;
        public final PasswordEncoder passwordEncoder;
        public final JwtTokenProvider jwtTokenProvider;

        public UserResponse signup(SignupRequest request) {
            if (userRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
            }

            if (userRepository.existsByNickname(request.nickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다");
            }

            User user = User.builder()
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .nickname(request.nickname())
            .build();

            User savedUser = userRepository.save(user);

            return UserResponse.from(savedUser);
        }
        @Transactional(readOnly = true)
        public LoginResponse login(LoginRequest request) {
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다"));

                if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                    throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
                }


                String accessToken = jwtTokenProvider.createAccessToken(user);

                return LoginResponse.of(accessToken, user);
         }
}
