package com.enofex.taikai.demo.task

import org.springframework.stereotype.Service
import java.util.UUID

@Service
internal class TaskService(private val repository: TaskRepository) {
    fun create(title: String): Task = repository.save(Task(UUID.randomUUID(), title.trim()))
    fun findAll(): List<Task> = repository.findAll()
    fun findById(id: UUID): Task? = repository.findById(id)
}
