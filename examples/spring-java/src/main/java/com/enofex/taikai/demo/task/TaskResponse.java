package com.enofex.taikai.demo.task;

import java.util.UUID;

record TaskResponse(UUID id, String title) {

  static TaskResponse of(Task task) {
    return new TaskResponse(task.id(), task.title());
  }
}
