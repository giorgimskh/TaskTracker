package com.george.task_tracker_cli.mappers.impl;

import com.george.task_tracker_cli.domain.dto.TaskDto;
import com.george.task_tracker_cli.domain.entities.Task;
import com.george.task_tracker_cli.domain.entities.TaskPriority;
import com.george.task_tracker_cli.domain.entities.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperImplTest {

    private final TaskMapperImpl taskMapper = new TaskMapperImpl();

    @Test
    void fromDtoCopiesFieldsAndLeavesServerFieldsEmpty() {
        UUID id = UUID.randomUUID();
        TaskDto dto = new TaskDto(id, "title", "description", LocalDate.of(2026, 1, 1),
                TaskPriority.HIGH, TaskStatus.CLOSED);

        Task task = taskMapper.fromDto(dto);

        assertEquals(id, task.getId());
        assertEquals("title", task.getTitle());
        assertEquals("description", task.getDescription());
        assertEquals(LocalDate.of(2026, 1, 1), task.getDueDate());
        assertEquals(TaskPriority.HIGH, task.getPriority());
        assertEquals(TaskStatus.CLOSED, task.getStatus());
        assertNull(task.getCreated());
        assertNull(task.getUpdated());
        assertNull(task.getTaskList());
    }

    @Test
    void toDtoCopiesFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task(id, "title", "description", LocalDate.of(2026, 1, 1),
                TaskStatus.OPEN, TaskPriority.LOW, now, now, null);

        TaskDto dto = taskMapper.toDto(task);

        assertEquals(new TaskDto(id, "title", "description", LocalDate.of(2026, 1, 1),
                TaskPriority.LOW, TaskStatus.OPEN), dto);
    }
}
