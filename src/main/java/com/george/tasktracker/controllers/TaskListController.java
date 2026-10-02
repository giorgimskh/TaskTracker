package com.george.tasktracker.controllers;

import com.george.tasktracker.services.TaskListService;
import com.george.tasktracker.domain.dto.TaskListDto;
import com.george.tasktracker.domain.entities.TaskList;
import com.george.tasktracker.mappers.TaskListMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/task-lists")
public class TaskListController {
    private final TaskListService taskListService;
    private final TaskListMapper taskListMappers;

    public TaskListController(TaskListService taskListService, TaskListMapper taskListMappers1) {
        this.taskListService = taskListService;
        this.taskListMappers = taskListMappers1;

    }

    @GetMapping
    public List<TaskListDto> listTaskLists() {
        return taskListService.listTaskLists()
                .stream()
                .map(taskListMappers::toDto).toList();
    }

    @PostMapping
    public TaskListDto createTaskList(@RequestBody TaskListDto taskListDto) {
        TaskList createdTaskList = taskListService.createTaskList(taskListMappers.fromDto(taskListDto));
        return taskListMappers.toDto(createdTaskList);
    }

    @GetMapping(path = "/{task_list_id}")
    public Optional<TaskListDto> getTaskList(@PathVariable("task_list_id")UUID taskListId) {
        return taskListService.getTaskList(taskListId)
                .map(taskListMappers::toDto);
    }

@PutMapping(path = "/{task_list_id}")
    public TaskListDto updateTaskList(
            @PathVariable("task_list_id")UUID taskListId,
            @RequestBody TaskListDto taskListDto){

        TaskList updatedTaskList= taskListService.updateTaskList(taskListId, taskListMappers.fromDto(taskListDto));
        return taskListMappers.toDto(updatedTaskList);
    }

    @DeleteMapping(path = "/{task_list_id}")
    public void deleteTaskList(@PathVariable("task_list_id")UUID taskListId) {
        taskListService.deleteTaskList(taskListId);
    }

}
