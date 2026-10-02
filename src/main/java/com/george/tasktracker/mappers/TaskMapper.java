package com.george.tasktracker.mappers;

import com.george.tasktracker.domain.dto.TaskDto;
import com.george.tasktracker.domain.entities.Task;

public interface TaskMapper {
    Task fromDto(TaskDto dto);
    TaskDto toDto(Task task);
}
