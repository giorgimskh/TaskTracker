package com.george.tasktracker.services.impl;

import com.george.tasktracker.repositories.TaskListRepository;
import com.george.tasktracker.services.TaskListService;
import com.george.tasktracker.domain.entities.TaskList;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskListServiceImpl implements TaskListService {

    private final TaskListRepository taskListRepository;

    public TaskListServiceImpl(TaskListRepository taskListRepository) {
        this.taskListRepository = taskListRepository;
    }


    @Override
    public List<TaskList> listTaskLists() {
        return taskListRepository.findAll();
    }

    @Override
    public TaskList createTaskList(TaskList taskList) {
        if(null!=taskList.getId()){
            throw new IllegalArgumentException("Task list already has an ID!");
        }
        if(null==taskList.getTitle() || taskList.getTitle().isBlank()){
            throw new IllegalArgumentException("Task list title is required!");
        }

        LocalDateTime now = LocalDateTime.now();

        return taskListRepository.save(new TaskList(
                null,
                taskList.getTitle(),
                taskList.getDescription(),
                null,
                now,
                now
        ));

    }

    @Override
    public Optional<TaskList> getTaskList(UUID Id) {
        return taskListRepository.findById(Id);
    }

    @Override
    public TaskList updateTaskList(UUID taskListId, TaskList taskList) {
        if(null==taskList.getId()){
            throw new IllegalArgumentException("Task list must have an  ID!");
        }

        if(!Objects.equals(taskList.getId(), taskListId)){
            throw new IllegalArgumentException("not permitted to change Task list ID !");
        }
        TaskList existingTaskList = taskListRepository.findById(taskListId).orElseThrow(()
                -> new IllegalArgumentException("task list not found!"));

        existingTaskList.setTitle(taskList.getTitle());
        existingTaskList.setDescription(taskList.getDescription());
        existingTaskList.setUpdated(LocalDateTime.now());

        return  taskListRepository.save(existingTaskList);
    }

    @Override
    public void deleteTaskList(UUID taskListId) {
        taskListRepository.deleteById(taskListId);
    }


}
