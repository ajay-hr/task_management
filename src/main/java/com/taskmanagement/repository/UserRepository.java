package com.taskmanagement.repository;

import com.taskmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUserCodeIgnoreCase(String userCode);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUserCode(String userCode);
}
