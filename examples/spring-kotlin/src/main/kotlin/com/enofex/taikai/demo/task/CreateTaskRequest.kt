package com.enofex.taikai.demo.task

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

internal data class CreateTaskRequest(
    @field:NotBlank @field:Size(max = 200) val title: String,
)
