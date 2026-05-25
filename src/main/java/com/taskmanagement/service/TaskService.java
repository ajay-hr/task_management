package com.taskmanagement.service;

import com.taskmanagement.dto.TaskRequest;
import com.taskmanagement.dto.TaskResponse;
import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.ApiException;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final CurrentUserService currentUserService;

    @Transactional
    public TaskResponse create(Long projectId, TaskRequest request) {
        Project project = projectService.getProjectForCurrentUser(projectId);
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .dueDate(request.dueDate())
                .project(project)
                .build();
        task.setAssignees(resolveAssignees(project, request.assignedToIds(), request.assignedToUserCodes(), request.assignedToEmails()));
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getByProject(Long projectId) {
        Project project = projectService.getProjectForCurrentUser(projectId);
        return taskRepository.findByProject(project).stream().map(TaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getByProject(Long projectId, Pageable pageable) {
        Project project = projectService.getProjectForCurrentUser(projectId);
        return taskRepository.findByProject(project, pageable).map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        return TaskResponse.from(getTaskForCurrentUser(id));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = getTaskForCurrentUser(id);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setDueDate(request.dueDate());
        task.setAssignees(resolveAssignees(task.getProject(), request.assignedToIds(), request.assignedToUserCodes(), request.assignedToEmails()));
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id) {
        Task task = getTaskForCurrentUser(id);
        taskRepository.delete(task);
    }

    private Task getTaskForCurrentUser(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
        if (!projectService.canAccess(task.getProject(), currentUserService.getCurrentUser())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this task");
        }
        return task;
    }

    private Set<User> resolveAssignees(Project project, Set<Long> ids, Set<String> codes, Set<String> emails) {
        Set<User> potentialAssignees = projectService.resolveUsers(ids, codes, emails);
        for (User assignee : potentialAssignees) {
            if (!projectService.canAccess(project, assignee)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Assigned user " + assignee.getEmail() + " must be a member of the project");
            }
        }
        return potentialAssignees;
    }
}
