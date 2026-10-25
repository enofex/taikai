package com.enofex.taikai.demo.task

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskHttpTest {
    @LocalServerPort private var port: Int = 0
    private val client = HttpClient.newHttpClient()
    private val json = JsonMapper()

    private fun get(path: String): HttpResponse<String> = client.send(
        HttpRequest.newBuilder(URI("http://localhost:$port$path")).GET().build(),
        HttpResponse.BodyHandlers.ofString())

    private fun post(body: String): HttpResponse<String> = client.send(
        HttpRequest.newBuilder(URI("http://localhost:$port/tasks"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
        HttpResponse.BodyHandlers.ofString())

    @Test
    fun `create list and retrieve tasks over HTTP`() {
        val created = post("""{"title":"  Learn Taikai  "}""")
        assertEquals(201, created.statusCode(), created.body())
        val task = json.readTree(created.body())
        assertEquals("Learn Taikai", task.get("title").asString())
        val id = UUID.fromString(task.get("id").asString())
        val retrieved = get("/tasks/$id")
        assertEquals(200, retrieved.statusCode())
        assertEquals(task, json.readTree(retrieved.body()))
        val listed = get("/tasks")
        assertEquals(200, listed.statusCode())
        assertTrue(json.readTree(listed.body()).any { it.get("id").asString() == id.toString() })
    }

    @Test
    fun `invalid requests and missing tasks have explicit HTTP responses`() {
        assertEquals(400, post("""{"title":"   "}""").statusCode())
        assertEquals(400, post("""{}""").statusCode())
        assertEquals(400, post("not-json").statusCode())
        assertEquals(400, post("""{"title":"${"a".repeat(201)}"}""").statusCode())
        assertEquals(404, get("/tasks/${UUID.randomUUID()}").statusCode())
        assertEquals(400, get("/tasks/not-a-uuid").statusCode())
    }
}
