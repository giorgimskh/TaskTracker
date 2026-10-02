package com.george.tasktracker.mappers;

import com.george.tasktracker.domain.dto.TaskListDto;
import com.george.tasktracker.domain.entities.TaskList;

public interface TaskListMapper {
    TaskList fromDto(TaskListDto taskListDto);
    TaskListDto toDto (TaskList taskList);
}
