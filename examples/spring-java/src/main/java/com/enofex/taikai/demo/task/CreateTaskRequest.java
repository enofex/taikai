package com.enofex.taikai.demo.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreateTaskRequest(@NotBlank @Size(max = 200) String title) {
}
