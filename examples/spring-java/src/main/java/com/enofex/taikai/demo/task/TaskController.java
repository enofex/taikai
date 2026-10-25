package com.enofex.taikai.demo.task;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/tasks")
class TaskController {

  private final TaskService service;

  TaskController(TaskService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  TaskResponse create(@Valid @RequestBody CreateTaskRequest request) {
    return TaskResponse.of(this.service.create(request.title()));
  }

  @GetMapping
  List<TaskResponse> list() {
    return this.service.findAll().stream()
        .map(TaskResponse::of)
        .toList();
  }

  @GetMapping("/{id}")
  TaskResponse get(@PathVariable UUID id) {
    return this.service.findById(id)
        .map(TaskResponse::of)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
  }
}
