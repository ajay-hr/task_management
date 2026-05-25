package com.taskmanagement.scheduler;

import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.TaskStatus;
import com.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskStatusScheduler {

    private final TaskRepository taskRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void updateOverdueTasks() {
        log.info("Running scheduled task to check for overdue tasks");
        List<Task> tasks = taskRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        for (Task task : tasks) {
            if (task.getStatus() != TaskStatus.DONE && 
                task.getStatus() != TaskStatus.OVERDUE && 
                task.getDueDate() != null && 
                task.getDueDate().isBefore(now)) {
                
                log.info("Task {} is overdue. Updating status.", task.getId());
                task.setStatus(TaskStatus.OVERDUE);
                taskRepository.save(task);
            }
        }
    }
}
