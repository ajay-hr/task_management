package com.taskmanagement.repository;

import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    Page<Task> findByProject(Project project, Pageable pageable);
    List<Task> findByStatusNotAndDueDateBetween(TaskStatus status, LocalDateTime start, LocalDateTime end);
}
