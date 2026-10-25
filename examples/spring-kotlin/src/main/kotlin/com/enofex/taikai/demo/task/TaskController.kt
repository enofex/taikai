package com.enofex.taikai.demo.task

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/tasks")
internal class TaskController(private val service: TaskService) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateTaskRequest): TaskResponse =
        service.create(request.title).let { TaskResponse(it.id, it.title) }

    @GetMapping
    fun list(): List<TaskResponse> = service.findAll().map { TaskResponse(it.id, it.title) }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): TaskResponse = service.findById(id)
        ?.let { TaskResponse(it.id, it.title) }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found")
}
