package com.george.tasktracker.services.impl;

import com.george.tasktracker.repositories.TaskListRepository;
import com.george.tasktracker.repositories.TaskRepository;
import com.george.tasktracker.services.TaskService;
import com.george.tasktracker.domain.entities.Task;
import com.george.tasktracker.domain.entities.TaskList;
import com.george.tasktracker.domain.entities.TaskPriority;
import com.george.tasktracker.domain.entities.TaskStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;

    public TaskServiceImpl(TaskRepository taskRepository, TaskListRepository taskListRepository) {
        this.taskRepository = taskRepository;
        this.taskListRepository = taskListRepository;
    }

    @Override
    public List<Task> ListTasks(UUID taskListId) {
        return taskRepository.findByTaskListId(taskListId);
    }

    @Override
    public Task createTask(UUID taskListId, Task task) {
        if(null!=task.getId()){
            throw new IllegalArgumentException("Task  already has an ID!");
        }
        if(null==task.getTitle() || task.getTitle().isEmpty()){
            throw new IllegalArgumentException("Task  must have a title!");
        }
        TaskPriority taskPriority = Optional.ofNullable(task.getPriority()).orElse(TaskPriority.MEDIUM);
        TaskStatus taskStatus = TaskStatus.OPEN;


        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID"));

        LocalDateTime dateTime = LocalDateTime.now();

        Task taskToSave = new Task(
                null,
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                taskStatus,
                taskPriority,
                dateTime,
                dateTime,
                taskList
        );

        return taskRepository.save(taskToSave);
    }

    @Override
    public Optional<Task> getTask(UUID taskListId, UUID taskId) {
        return taskRepository.findByTaskListIdAndId(taskListId, taskId);
    }

    @Override
    public Task updateTask(UUID taskListId, UUID taskId, Task task) {
        if(null==task.getId()){
            throw new IllegalArgumentException("Task  must have an ID!");
        }
        if(!Objects.equals(taskId, task.getId())){
            throw new IllegalArgumentException("Task IDs do not match");
        }
        if(null==task.getTitle() || task.getTitle().isEmpty()){
            throw new IllegalArgumentException("Task  must have a title!");
        }
        if(null==task.getPriority()){
            throw new IllegalArgumentException("Task  must have a priority!");
        }
        if(null==task.getStatus()){
            throw new IllegalArgumentException("Task  must have a status!");
        }

        Task existingTask=taskRepository.findByTaskListIdAndId(taskListId, taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setDueDate(task.getDueDate());
        existingTask.setPriority(task.getPriority());
        existingTask.setStatus(task.getStatus());
        existingTask.setUpdated(LocalDateTime.now());

        return taskRepository.save(existingTask);
    }

    @Transactional
    @Override
    public void deleteTask(UUID taskListId, UUID taskId) {
        taskRepository.deleteByTaskListIdAndId(taskListId, taskId);
    }
}
