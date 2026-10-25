package com.enofex.taikai.demo.web

import com.enofex.taikai.demo.service.TaskService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

data class CreateTaskRequest(
    @field:NotBlank @field:Size(max = 200) val title: String,
)
data class TaskResponse(val id: UUID, val title: String)

@RestController
@RequestMapping("/tasks")
class TaskController(private val service: TaskService) {
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
