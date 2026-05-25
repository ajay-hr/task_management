package com.taskmanagement.controller;

import com.taskmanagement.dto.TaskRequest;
import com.taskmanagement.dto.TaskResponse;
import com.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/api/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@PathVariable Long projectId, @Valid @RequestBody TaskRequest request) {
        return taskService.create(projectId, request);
    }

    @GetMapping("/api/projects/{projectId}/tasks")
    public List<TaskResponse> getByProject(@PathVariable Long projectId) {
        return taskService.getByProject(projectId);
    }

    @GetMapping("/api/projects/{projectId}/tasks/paged")
    public Page<TaskResponse> getByProjectPaged(@PathVariable Long projectId, Pageable pageable) {
        return taskService.getByProject(projectId, pageable);
    }

    @GetMapping("/api/tasks/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @PutMapping("/api/tasks/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.update(id, request);
    }

    @DeleteMapping("/api/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }
}
