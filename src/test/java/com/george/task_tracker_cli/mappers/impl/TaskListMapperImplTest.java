package com.george.task_tracker_cli.mappers.impl;

import com.george.task_tracker_cli.domain.dto.TaskDto;
import com.george.task_tracker_cli.domain.dto.TaskListDto;
import com.george.task_tracker_cli.domain.entities.Task;
import com.george.task_tracker_cli.domain.entities.TaskList;
import com.george.task_tracker_cli.domain.entities.TaskPriority;
import com.george.task_tracker_cli.domain.entities.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskListMapperImplTest {

    private final TaskListMapperImpl taskListMapper = new TaskListMapperImpl(new TaskMapperImpl());

    private Task task(String title, TaskStatus status) {
        return new Task(UUID.randomUUID(), title, null, null, status, TaskPriority.MEDIUM, null, null, null);
    }

    @Test
    void toDtoCountsTasksAndCalculatesProgress() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        TaskList taskList = new TaskList(id, "list", "description",
                List.of(task("done", TaskStatus.CLOSED), task("todo", TaskStatus.OPEN)), now, now);

        TaskListDto dto = taskListMapper.toDto(taskList);

        assertEquals(id, dto.id());
        assertEquals("list", dto.title());
        assertEquals("description", dto.description());
        assertEquals(2, dto.count());
        assertEquals(0.5, dto.progress());
        assertEquals(List.of("done", "todo"), dto.tasks().stream().map(TaskDto::title).toList());
    }

    @Test
    void toDtoWithoutTasksHasZeroCountAndNoProgress() {
        TaskList taskList = new TaskList(UUID.randomUUID(), "list", null, null, null, null);

        TaskListDto dto = taskListMapper.toDto(taskList);

        assertEquals(0, dto.count());
        assertNull(dto.progress());
        assertNull(dto.tasks());
    }

    @Test
    void fromDtoMapsTasksAndLeavesTimestampsEmpty() {
        UUID id = UUID.randomUUID();
        TaskDto taskDto = new TaskDto(UUID.randomUUID(), "task", null, null, TaskPriority.LOW, TaskStatus.OPEN);
        TaskListDto dto = new TaskListDto(id, "list", "description", null, null, List.of(taskDto));

        TaskList taskList = taskListMapper.fromDto(dto);

        assertEquals(id, taskList.getId());
        assertEquals("list", taskList.getTitle());
        assertEquals("description", taskList.getDescription());
        assertEquals(1, taskList.getTasks().size());
        assertEquals("task", taskList.getTasks().getFirst().getTitle());
        assertNull(taskList.getCreated());
        assertNull(taskList.getUpdated());
    }

    @Test
    void fromDtoWithoutTasksLeavesTasksEmpty() {
        TaskListDto dto = new TaskListDto(null, "list", null, null, null, null);

        assertNull(taskListMapper.fromDto(dto).getTasks());
    }
}
