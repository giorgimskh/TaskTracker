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
class TaskListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskListRepository taskListRepository;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        taskListRepository.deleteAll();
    }

    private String createTaskList(String title) throws Exception {
        String response = mockMvc.perform(post("/api/task-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"description\":\"description\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private String createTask(String taskListId, String title) throws Exception {
        String response = mockMvc.perform(post("/task_list/" + taskListId + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void createTaskListReturnsCreatedList() throws Exception {
        mockMvc.perform(post("/api/task-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Groceries\",\"description\":\"weekly\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Groceries"))
                .andExpect(jsonPath("$.description").value("weekly"))
                .andExpect(jsonPath("$.count").value(0));

        assertEquals(1, taskListRepository.count());
    }

    @Test
    void createTaskListWithoutTitleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/task-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"no title\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Task list title is required!"));

        assertEquals(0, taskListRepository.count());
    }

    @Test
    void listTaskListsReturnsAllLists() throws Exception {
        createTaskList("first");
        createTaskList("second");

        mockMvc.perform(get("/api/task-lists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getTaskListReturnsListWithTasksAndProgress() throws Exception {
        String taskListId = createTaskList("list");
        String doneTaskId = createTask(taskListId, "done");
        createTask(taskListId, "todo");
        mockMvc.perform(put("/task_list/" + taskListId + "/tasks/" + doneTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + doneTaskId + "\",\"title\":\"done\",\"priority\":\"MEDIUM\",\"status\":\"CLOSED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/task-lists/" + taskListId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskListId))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.progress").value(0.5))
                .andExpect(jsonPath("$.tasks.length()").value(2));
    }

    @Test
    void updateTaskListUpdatesTitleAndDescription() throws Exception {
        String taskListId = createTaskList("old");

        mockMvc.perform(put("/api/task-lists/" + taskListId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + taskListId + "\",\"title\":\"new\",\"description\":\"changed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("new"))
                .andExpect(jsonPath("$.description").value("changed"));
    }

    @Test
    void updateTaskListWithMismatchedIdReturnsBadRequest() throws Exception {
        String taskListId = createTaskList("list");

        mockMvc.perform(put("/api/task-lists/" + taskListId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"00000000-0000-0000-0000-000000000000\",\"title\":\"new\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTaskListRemovesListAndItsTasks() throws Exception {
        String taskListId = createTaskList("list");
        createTask(taskListId, "task");

        mockMvc.perform(delete("/api/task-lists/" + taskListId))
                .andExpect(status().isOk());

        assertEquals(0, taskListRepository.count());
        assertEquals(0, taskRepository.count());
    }
}
