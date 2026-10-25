package com.enofex.taikai.demo.task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class TaskService {

  private final TaskRepository repository;

  TaskService(TaskRepository repository) {
    this.repository = repository;
  }

  Task create(String title) {
    return this.repository.save(new Task(UUID.randomUUID(), title.trim()));
  }

  List<Task> findAll() {
    return this.repository.findAll();
  }

  Optional<Task> findById(UUID id) {
    return this.repository.findById(id);
  }
}
