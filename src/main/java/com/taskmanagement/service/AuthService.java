package com.taskmanagement.service;

import com.taskmanagement.dto.AuthResponse;
import com.taskmanagement.dto.LoginRequest;
import com.taskmanagement.dto.RefreshTokenRequest;
import com.taskmanagement.dto.RegisterRequest;
import com.taskmanagement.dto.UserResponse;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.ApiException;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final UserCodeService userCodeService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .userCode(userCodeService.generateUniqueCode())
                .password(request.password())
                .role("USER")
                .build();
        user.setPassword(encoder.encode(user.getPassword()));

        return tokensFor(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email"));

        if (!encoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return tokensFor(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        if (!jwtUtil.isRefreshToken(request.refreshToken())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token required");
        }

        String email = jwtUtil.extractEmail(request.refreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (!jwtUtil.isTokenValid(request.refreshToken(), user.getEmail())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        return tokensFor(user);
    }

    private AuthResponse tokensFor(User user) {
        if (user.getUserCode() == null || user.getUserCode().isBlank()) {
            user.setUserCode(userCodeService.generateUniqueCode());
            user = userRepository.save(user);
        }

        return new AuthResponse(
                jwtUtil.generateAccessToken(user.getEmail()),
                jwtUtil.generateRefreshToken(user.getEmail()),
                "Bearer",
                UserResponse.from(user)
        );
    }
}
