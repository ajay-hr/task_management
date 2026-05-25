package com.taskmanagement.service;

import com.taskmanagement.dto.ProjectRequest;
import com.taskmanagement.dto.ProjectResponse;
import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.ApiException;
import com.taskmanagement.repository.ProjectRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @CacheEvict(value = "projects", allEntries = true)
    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        User owner = currentUserService.getCurrentUser();
        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .owner(owner)
                .build();
        project.setMembers(resolveUsers(request.memberIds(), request.memberUserCodes(), request.memberEmails()));
        project.getAdmins().add(owner);
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Cacheable(value = "projects", key = "#currentUserService.getCurrentUser().email")
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllForCurrentUser() {
        User user = currentUserService.getCurrentUser();
        return projectRepository.findDistinctByOwnerOrMembersContaining(user, user)
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAllForCurrentUser(Pageable pageable) {
        User user = currentUserService.getCurrentUser();
        return projectRepository.findDistinctByOwnerOrMembersContaining(user, user, pageable)
                .map(ProjectResponse::from);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(Long id) {
        return ProjectResponse.from(getProjectForCurrentUser(id));
    }

    @CacheEvict(value = "projects", allEntries = true)
    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = getProjectOwnedByCurrentUser(id);
        project.setName(request.name());
        project.setDescription(request.description());
        project.setMembers(resolveUsers(request.memberIds(), request.memberUserCodes(), request.memberEmails()));
        return ProjectResponse.from(projectRepository.save(project));
    }

    @CacheEvict(value = "projects", allEntries = true)
    @Transactional
    public void delete(Long id) {
        Project project = getProjectOwnedByCurrentUser(id);
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public Project getProjectForCurrentUser(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
        User user = currentUserService.getCurrentUser();
        if (!canAccess(project, user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this project");
        }
        return project;
    }

    public boolean canAccess(Project project, User user) {
        return project.getOwner().getId().equals(user.getId())
                || project.getAdmins().stream().anyMatch(admin -> admin.getId().equals(user.getId()))
                || project.getMembers().stream().anyMatch(member -> member.getId().equals(user.getId()));
    }

    private Project getProjectOwnedByCurrentUser(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
        User user = currentUserService.getCurrentUser();
        boolean isAdmin = project.getOwner().getId().equals(user.getId()) 
                || project.getAdmins().stream().anyMatch(admin -> admin.getId().equals(user.getId()));
        if (!isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only project admins can modify this project");
        }
        return project;
    }

    public Set<User> resolveUsers(Set<Long> ids, Set<String> codes, Set<String> emails) {
        Set<User> users = new HashSet<>();
        if (ids != null && !ids.isEmpty()) users.addAll(userRepository.findAllById(ids));
        if (codes != null && !codes.isEmpty()) {
            for (String code : codes) {
                if (code != null && !code.isBlank()) userRepository.findByUserCodeIgnoreCase(code.trim()).ifPresent(users::add);
            }
        }
        if (emails != null && !emails.isEmpty()) {
            for (String email : emails) {
                if (email != null && !email.isBlank()) userRepository.findByEmailIgnoreCase(email.trim()).ifPresent(users::add);
            }
        }
        return users;
    }
}
