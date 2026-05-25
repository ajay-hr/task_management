package com.taskmanagement.repository;

import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    java.util.List<Project> findDistinctByOwnerOrMembersContaining(User owner, User member);
    Page<Project> findDistinctByOwnerOrMembersContaining(User owner, User member, Pageable pageable);
}
