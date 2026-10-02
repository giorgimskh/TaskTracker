package com.george.tasktracker.controllers;

import com.george.tasktracker.services.TaskService;
import com.george.tasktracker.domain.dto.TaskDto;
import com.george.tasktracker.domain.entities.Task;
import com.george.tasktracker.mappers.TaskMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/task_list/{task_list_id}/tasks")
public class TasksController {
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TasksController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public List<TaskDto> ListTasks(@PathVariable("task_list_id") UUID taskListId) {
        return taskService.ListTasks(taskListId).stream().map(taskMapper::toDto).toList();
    }

    @PostMapping
    public TaskDto createTask(@PathVariable("task_list_id") UUID taskListId,@RequestBody TaskDto taskDto) {
        Task createdTask= taskService.createTask(taskListId,taskMapper.fromDto(taskDto));
        return taskMapper.toDto(createdTask);
    }

    @GetMapping(path = "/{task_id}")
    public Optional<TaskDto> getTask(@PathVariable("task_list_id") UUID taskListId,
                                  @PathVariable("task_id")UUID taskId){
        return taskService.getTask(taskListId,taskId).map(taskMapper::toDto);
    }

    @PutMapping(path = "/{task_id}")
    public TaskDto updateTask(
            @PathVariable("task_list_id")UUID taskListId,
            @PathVariable("task_id")UUID taskId,
            @RequestBody TaskDto taskDto
    ){
        Task updatedTask= taskService.updateTask(taskListId,taskId,taskMapper.fromDto(taskDto));
        return taskMapper.toDto(updatedTask);
    }

    @DeleteMapping(path = "/{task_id}")
    public void deleteTask(@PathVariable("task_list_id")UUID taskListId, @PathVariable("task_id")UUID taskId){
        taskService.deleteTask(taskListId,taskId);
    }
}
