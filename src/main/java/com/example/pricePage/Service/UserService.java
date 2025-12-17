package com.example.pricePage.Service;

import com.example.pricePage.Dto.UserRequest;
import com.example.pricePage.Dto.UserResponse;
import com.example.pricePage.Entity.User;
import com.example.pricePage.Repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /** 내 정보 조회 */
    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserResponse.from(user);
    }

    /** ✅ 프로필 동기화 (이름) */
    @Transactional
    public UserResponse syncProfile(Long userId, UserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.syncProfile(request.getName());

        return UserResponse.from(user);
    }
}