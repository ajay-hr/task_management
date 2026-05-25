package com.taskmanagement.service;

import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminBootstrapService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserCodeService userCodeService;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }

        User admin = User.builder()
                .name("Administrator")
                .email(adminEmail.toLowerCase())
                .userCode(userCodeService.generateUniqueCode())
                .password(passwordEncoder.encode(adminPassword))
                .role("ADMIN")
                .build();

        userRepository.save(admin);
    }
}
