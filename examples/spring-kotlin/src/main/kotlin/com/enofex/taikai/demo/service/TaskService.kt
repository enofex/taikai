package com.enofex.taikai.demo.service

import com.enofex.taikai.demo.model.Task
import com.enofex.taikai.demo.repository.TaskRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TaskService(private val repository: TaskRepository) {
    fun create(title: String): Task = repository.save(Task(UUID.randomUUID(), title.trim()))
    fun findAll(): List<Task> = repository.findAll()
    fun findById(id: UUID): Task? = repository.findById(id)
}
