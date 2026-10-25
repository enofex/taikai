package com.enofex.taikai.demo.task

import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
internal class TaskRepository {
    private val tasks = ConcurrentHashMap<UUID, Task>()

    fun save(task: Task): Task = task.also { tasks[it.id] = it }
    fun findAll(): List<Task> = tasks.values.sortedBy { it.id }
    fun findById(id: UUID): Task? = tasks[id]
}
