package com.enofex.taikai.demo.task;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
class TaskRepository {

  private final Map<UUID, Task> tasks = new ConcurrentHashMap<>();

  Task save(Task task) {
    this.tasks.put(task.id(), task);
    return task;
  }

  List<Task> findAll() {
    return this.tasks.values().stream()
        .sorted(Comparator.comparing(Task::id))
        .toList();
  }

  Optional<Task> findById(UUID id) {
    return Optional.ofNullable(this.tasks.get(id));
  }
}
