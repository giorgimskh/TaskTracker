package com.george.task_tracker_cli.Services.Impl;

import com.george.task_tracker_cli.Repositories.TaskListRepository;
import com.george.task_tracker_cli.domain.entities.TaskList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskListServiceImplTest {

    @Mock
    private TaskListRepository taskListRepository;

    @InjectMocks
    private TaskListServiceImpl taskListService;

    private final UUID taskListId = UUID.randomUUID();

    @Test
    void listTaskListsReturnsAllLists() {
        List<TaskList> taskLists = List.of(new TaskList(taskListId, "list", null, null, null, null));
        when(taskListRepository.findAll()).thenReturn(taskLists);

        assertEquals(taskLists, taskListService.listTaskLists());
    }

    @Test
    void createTaskListSavesListWithTimestamps() {
        when(taskListRepository.save(any(TaskList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskList created = taskListService.createTaskList(
                new TaskList(null, "list", "description", List.of(), null, null));

        assertNull(created.getId());
        assertEquals("list", created.getTitle());
        assertEquals("description", created.getDescription());
        assertNull(created.getTasks());
        assertNotNull(created.getCreated());
        assertEquals(created.getCreated(), created.getUpdated());
    }

    @Test
    void createTaskListRejectsListWithId() {
        assertThrows(IllegalArgumentException.class,
                () -> taskListService.createTaskList(new TaskList(taskListId, "list", null, null, null, null)));
        verify(taskListRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void createTaskListRejectsBlankTitle(String title) {
        assertThrows(IllegalArgumentException.class,
                () -> taskListService.createTaskList(new TaskList(null, title, null, null, null, null)));
        verify(taskListRepository, never()).save(any());
    }

    @Test
    void getTaskListLooksUpListById() {
        TaskList taskList = new TaskList(taskListId, "list", null, null, null, null);
        when(taskListRepository.findById(taskListId)).thenReturn(Optional.of(taskList));

        assertEquals(Optional.of(taskList), taskListService.getTaskList(taskListId));
    }

    @Test
    void updateTaskListUpdatesExistingList() {
        LocalDateTime created = LocalDateTime.of(2025, 1, 1, 0, 0);
        TaskList existing = new TaskList(taskListId, "old", "old description", null, created, created);
        when(taskListRepository.findById(taskListId)).thenReturn(Optional.of(existing));
        when(taskListRepository.save(any(TaskList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskList updated = taskListService.updateTaskList(taskListId,
                new TaskList(taskListId, "new", "new description", null, null, null));

        assertEquals("new", updated.getTitle());
        assertEquals("new description", updated.getDescription());
        assertEquals(created, updated.getCreated());
        assertTrue(updated.getUpdated().isAfter(created));
    }

    @Test
    void updateTaskListRejectsMissingId() {
        TaskList taskList = new TaskList(null, "list", null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> taskListService.updateTaskList(taskListId, taskList));
        verify(taskListRepository, never()).save(any());
    }

    @Test
    void updateTaskListRejectsMismatchedId() {
        TaskList taskList = new TaskList(UUID.randomUUID(), "list", null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> taskListService.updateTaskList(taskListId, taskList));
        verify(taskListRepository, never()).save(any());
    }

    @Test
    void updateTaskListRejectsUnknownList() {
        when(taskListRepository.findById(taskListId)).thenReturn(Optional.empty());
        TaskList taskList = new TaskList(taskListId, "list", null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> taskListService.updateTaskList(taskListId, taskList));
        verify(taskListRepository, never()).save(any());
    }

    @Test
    void deleteTaskListDeletesById() {
        taskListService.deleteTaskList(taskListId);

        verify(taskListRepository).deleteById(taskListId);
    }
}
