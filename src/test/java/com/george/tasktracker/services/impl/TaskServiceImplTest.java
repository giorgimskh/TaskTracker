package com.george.tasktracker.services.impl;

import com.george.tasktracker.repositories.TaskListRepository;
import com.george.tasktracker.repositories.TaskRepository;
import com.george.tasktracker.domain.entities.Task;
import com.george.tasktracker.domain.entities.TaskList;
import com.george.tasktracker.domain.entities.TaskPriority;
import com.george.tasktracker.domain.entities.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskListRepository taskListRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private final UUID taskListId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();

    private Task task(UUID id, String title, TaskPriority priority, TaskStatus status) {
        return new Task(id, title, "description", LocalDate.of(2026, 1, 1), status, priority, null, null, null);
    }

    @Test
    void listTasksReturnsTasksOfTheList() {
        List<Task> tasks = List.of(task(taskId, "title", TaskPriority.LOW, TaskStatus.OPEN));
        when(taskRepository.findByTaskListId(taskListId)).thenReturn(tasks);

        assertEquals(tasks, taskService.ListTasks(taskListId));
    }

    @Test
    void createTaskSavesTaskWithDefaults() {
        TaskList taskList = new TaskList(taskListId, "list", null, null, null, null);
        when(taskListRepository.findById(taskListId)).thenReturn(Optional.of(taskList));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task created = taskService.createTask(taskListId, task(null, "title", null, TaskStatus.CLOSED));

        assertEquals("title", created.getTitle());
        assertEquals("description", created.getDescription());
        assertEquals(LocalDate.of(2026, 1, 1), created.getDueDate());
        assertEquals(TaskPriority.MEDIUM, created.getPriority());
        assertEquals(TaskStatus.OPEN, created.getStatus());
        assertSame(taskList, created.getTaskList());
        assertNotNull(created.getCreated());
        assertEquals(created.getCreated(), created.getUpdated());
    }

    @Test
    void createTaskKeepsGivenPriority() {
        when(taskListRepository.findById(taskListId))
                .thenReturn(Optional.of(new TaskList(taskListId, "list", null, null, null, null)));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task created = taskService.createTask(taskListId, task(null, "title", TaskPriority.HIGH, null));

        assertEquals(TaskPriority.HIGH, created.getPriority());
    }

    @Test
    void createTaskRejectsTaskWithId() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(taskListId, task(taskId, "title", null, null)));
        verify(taskRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createTaskRejectsMissingTitle(String title) {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(taskListId, task(null, title, null, null)));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTaskRejectsUnknownTaskList() {
        when(taskListRepository.findById(taskListId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(taskListId, task(null, "title", null, null)));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void getTaskLooksUpTaskWithinList() {
        Task existing = task(taskId, "title", TaskPriority.LOW, TaskStatus.OPEN);
        when(taskRepository.findByTaskListIdAndId(taskListId, taskId)).thenReturn(Optional.of(existing));

        assertEquals(Optional.of(existing), taskService.getTask(taskListId, taskId));
    }

    @Test
    void updateTaskUpdatesExistingTask() {
        LocalDateTime created = LocalDateTime.of(2025, 1, 1, 0, 0);
        Task existing = new Task(taskId, "old", "old description", null, TaskStatus.OPEN, TaskPriority.LOW,
                created, created, null);
        when(taskRepository.findByTaskListIdAndId(taskListId, taskId)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task updated = taskService.updateTask(taskListId, taskId,
                task(taskId, "new", TaskPriority.HIGH, TaskStatus.CLOSED));

        assertEquals("new", updated.getTitle());
        assertEquals("description", updated.getDescription());
        assertEquals(LocalDate.of(2026, 1, 1), updated.getDueDate());
        assertEquals(TaskPriority.HIGH, updated.getPriority());
        assertEquals(TaskStatus.CLOSED, updated.getStatus());
        assertEquals(created, updated.getCreated());
        assertTrue(updated.getUpdated().isAfter(created));
    }

    @Test
    void updateTaskRejectsMissingId() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTask(taskListId, taskId, task(null, "title", TaskPriority.LOW, TaskStatus.OPEN)));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskRejectsMismatchedId() {
        Task task = task(UUID.randomUUID(), "title", TaskPriority.LOW, TaskStatus.OPEN);

        assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(taskListId, taskId, task));
        verify(taskRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void updateTaskRejectsMissingTitle(String title) {
        Task task = task(taskId, title, TaskPriority.LOW, TaskStatus.OPEN);

        assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(taskListId, taskId, task));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskRejectsMissingPriority() {
        Task task = task(taskId, "title", null, TaskStatus.OPEN);

        assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(taskListId, taskId, task));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskRejectsMissingStatus() {
        Task task = task(taskId, "title", TaskPriority.LOW, null);

        assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(taskListId, taskId, task));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskRejectsUnknownTask() {
        when(taskRepository.findByTaskListIdAndId(taskListId, taskId)).thenReturn(Optional.empty());
        Task task = task(taskId, "title", TaskPriority.LOW, TaskStatus.OPEN);

        assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(taskListId, taskId, task));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void deleteTaskDeletesTaskWithinList() {
        taskService.deleteTask(taskListId, taskId);

        verify(taskRepository).deleteByTaskListIdAndId(taskListId, taskId);
    }
}
