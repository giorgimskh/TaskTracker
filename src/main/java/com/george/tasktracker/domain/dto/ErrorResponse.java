package com.george.tasktracker.domain.dto;

public record ErrorResponse(
    int status,
    String message,
    String details
){
}
