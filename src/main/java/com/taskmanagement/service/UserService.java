package com.taskmanagement.service;

import com.taskmanagement.dto.PasswordResetRequest;
import com.taskmanagement.dto.UserResponse;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse resetMyPassword(PasswordResetRequest request) {
        User user = currentUserService.getCurrentUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        return UserResponse.from(userRepository.save(user));
    }
}
