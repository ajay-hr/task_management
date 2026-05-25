package com.taskmanagement.service;

import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.TaskStatus;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.TaskRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeadlineReminderService {

    private final TaskRepository taskRepository;
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.reminders.enabled:true}")
    private boolean remindersEnabled;

    @Scheduled(cron = "${app.reminders.cron:0 0 * * * *}")
    public void sendReminders() {
        if (!remindersEnabled) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        List<Task> upcomingTasks = taskRepository.findByStatusNotAndDueDateBetween(
                TaskStatus.DONE, now, tomorrow);

        for (Task task : upcomingTasks) {
            for (User assignee : task.getAssignees()) {
                sendEmail(assignee, task);
            }
        }
    }

    private void sendEmail(User user, Task task) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Upcoming Task Deadline: " + task.getTitle());
            helper.setText(String.format(
                    "Hello %s,\n\nThis is a reminder that the task \"%s\" in project \"%s\" is due on %s.\n\nStatus: %s\n\nBest regards,\nTask Management System",
                    user.getName(), task.getTitle(), task.getProject().getName(), task.getDueDate(), task.getStatus()
            ));

            mailSender.send(message);
            log.info("Sent reminder for task {} to user {}", task.getId(), user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send reminder for task {} to user {}", task.getId(), user.getEmail(), e);
        }
    }
}
