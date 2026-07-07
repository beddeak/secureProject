package com.securearchive.archive.user;

import com.securearchive.archive.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
        public final UserRepository userRepository;

        public List<UserResponse> getUsers() {
            return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
        }
        public UserResponse getUserById(Long userId) {
            User user = userRepository.findById(userId)
                .orElseThrow();

            return UserResponse.from(user);
        }
}
