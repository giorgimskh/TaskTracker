package com.george.tasktracker.domain.dto;

import com.george.tasktracker.domain.entities.TaskPriority;
import com.george.tasktracker.domain.entities.TaskStatus;

import java.time.LocalDate;
import java.util.UUID;

public record TaskDto(
        UUID id,
        String title,
        String description,
        LocalDate dueDate,
        TaskPriority priority,
        TaskStatus status) {
}
