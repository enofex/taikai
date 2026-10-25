package com.enofex.taikai.demo.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskHttpTest {

  private final HttpClient client = HttpClient.newHttpClient();
  private final JsonMapper json = JsonMapper.builder().build();

  @LocalServerPort
  private int port;

  @Test
  void createListAndRetrieveTasksOverHttp() throws Exception {
    HttpResponse<String> created = post("{\"title\":\"  Learn Taikai  \"}");
    assertEquals(201, created.statusCode(), created.body());
    JsonNode task = this.json.readTree(created.body());
    assertEquals("Learn Taikai", task.get("title").asString());
    UUID id = UUID.fromString(task.get("id").asString());

    HttpResponse<String> retrieved = get("/tasks/" + id);
    assertEquals(200, retrieved.statusCode());
    assertEquals(task, this.json.readTree(retrieved.body()));

    HttpResponse<String> listed = get("/tasks");
    assertEquals(200, listed.statusCode());
    assertTrue(this.json.readTree(listed.body()).valueStream()
        .anyMatch(node -> node.get("id").asString().equals(id.toString())));
  }

  @Test
  void invalidRequestsAndMissingTasksHaveExplicitHttpResponses() throws Exception {
    assertEquals(400, post("{\"title\":\"   \"}").statusCode());
    assertEquals(400, post("{}").statusCode());
    assertEquals(400, post("not-json").statusCode());
    assertEquals(400, post("{\"title\":\"" + "a".repeat(201) + "\"}").statusCode());
    assertEquals(404, get("/tasks/" + UUID.randomUUID()).statusCode());
    assertEquals(400, get("/tasks/not-a-uuid").statusCode());
  }

  private HttpResponse<String> get(String path) throws IOException, InterruptedException {
    return this.client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + path)).GET().build(),
        HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> post(String body) throws IOException, InterruptedException {
    return this.client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + "/tasks"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }
}
