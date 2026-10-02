package com.george.tasktracker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.george.tasktracker.repositories.TaskListRepository;
import com.george.tasktracker.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TasksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskListRepository taskListRepository;

    private String taskListId;

    @BeforeEach
    void setUp() throws Exception {
        taskRepository.deleteAll();
        taskListRepository.deleteAll();
        taskListId = createTaskList("list");
    }

    private String createTaskList(String title) throws Exception {
        String response = mockMvc.perform(post("/api/task-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private String createTask(String taskListId, String title) throws Exception {
        String response = mockMvc.perform(post("/api/task-lists/" + taskListId + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void createTaskDefaultsToOpenAndMediumPriority() throws Exception {
        mockMvc.perform(post("/api/task-lists/" + taskListId + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Buy milk\",\"description\":\"2 litres\",\"dueDate\":\"2026-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.description").value("2 litres"))
                .andExpect(jsonPath("$.dueDate").value("2026-01-01"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void createTaskWithoutTitleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/task-lists/" + taskListId + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"no title\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertEquals(0, taskRepository.count());
    }

    @Test
    void createTaskForUnknownListReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/task-lists/00000000-0000-0000-0000-000000000000/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"task\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task list not found"));

        assertEquals(0, taskRepository.count());
    }

    @Test
    void listTasksReturnsOnlyTasksOfThatList() throws Exception {
        String otherTaskListId = createTaskList("other");
        createTask(taskListId, "first");
        createTask(taskListId, "second");
        createTask(otherTaskListId, "other");

        mockMvc.perform(get("/api/task-lists/" + taskListId + "/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getTaskReturnsTask() throws Exception {
        String taskId = createTask(taskListId, "task");

        mockMvc.perform(get("/api/task-lists/" + taskListId + "/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("task"));
    }

    @Test
    void updateTaskUpdatesAllFields() throws Exception {
        String taskId = createTask(taskListId, "old");

        mockMvc.perform(put("/api/task-lists/" + taskListId + "/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + taskId + "\",\"title\":\"new\",\"description\":\"changed\","
                                + "\"dueDate\":\"2026-02-01\",\"priority\":\"HIGH\",\"status\":\"CLOSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("new"))
                .andExpect(jsonPath("$.description").value("changed"))
                .andExpect(jsonPath("$.dueDate").value("2026-02-01"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void updateTaskWithMismatchedIdReturnsBadRequest() throws Exception {
        String taskId = createTask(taskListId, "task");

        mockMvc.perform(put("/api/task-lists/" + taskListId + "/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"00000000-0000-0000-0000-000000000000\",\"title\":\"new\","
                                + "\"priority\":\"HIGH\",\"status\":\"OPEN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Task IDs do not match"));
    }

    @Test
    void deleteTaskRemovesOnlyThatTask() throws Exception {
        String taskId = createTask(taskListId, "delete me");
        createTask(taskListId, "keep me");

        mockMvc.perform(delete("/api/task-lists/" + taskListId + "/tasks/" + taskId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/task-lists/" + taskListId + "/tasks"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("keep me"));
    }

    @Test
    void getUnknownTaskReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/task-lists/" + taskListId + "/tasks/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void getTaskFromOtherListReturnsNotFound() throws Exception {
        String otherTaskListId = createTaskList("other");
        String taskId = createTask(taskListId, "task");

        mockMvc.perform(get("/api/task-lists/" + otherTaskListId + "/tasks/" + taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUnknownTaskReturnsNotFound() throws Exception {
        String unknownId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(put("/api/task-lists/" + taskListId + "/tasks/" + unknownId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + unknownId + "\",\"title\":\"new\","
                                + "\"priority\":\"HIGH\",\"status\":\"OPEN\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));
    }
}
