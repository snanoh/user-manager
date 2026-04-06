package com.user.manage.service;

import com.user.manage.dto.response.UserInfoResponse;
import com.user.manage.entity.User;
import com.user.manage.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        return UserInfoResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserInfoResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserInfoResponse::from)
                .toList();
    }
}
