# Spring Kotlin demo

A small task API demonstrates Taikai's existing Java API from Kotlin, its receiver
DSL, shared Spring rules, and source-aware Kotlin rules. It uses Spring Boot,
constructor injection, Kotlin data classes, and a thread-safe in-memory repository.
Data resets when the application restarts.

From the repository root, using JDK 17 or later:

```sh
./mvnw verify
java -jar examples/spring-kotlin/target/taikai-spring-kotlin-demo-2.4.1-SNAPSHOT.jar
```

Use the version from the root POM if it changes. The server listens on port 8080.
To build only the demo and its dependencies:

```sh
./mvnw -pl examples/spring-kotlin -am package
```

Create and list tasks:

```sh
curl -i -X POST http://localhost:8080/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Learn Taikai Kotlin rules"}'
curl http://localhost:8080/tasks
```

Retrieve a task with `GET /tasks/{id}`, using the UUID returned by the create request.
Creation returns 201, retrieval/listing return 200, and an unknown task returns 404.
Blank titles, titles longer than 200 characters, malformed JSON, missing titles,
and malformed UUIDs return 400. Titles are trimmed before storage.

The source separates `web`, `service`, `repository`, and `model` packages.
Architecture tests enforce naming, data classes with immutable declared properties,
source function/property naming, negative naming rules, type-selected property naming,
deprecated API usage, parameter limits, package cycles, shaded dependencies, and layer boundaries. The
`violations` test package contains intentional failures; tests assert their messages
so a normal build stays green. Those Spring fixtures use an inactive profile and are
absent from the packaged application.

`TaskHttpTest` starts a real server on a random port and checks the HTTP behavior.
`ArchitectureTest` shows both the original Java builder from Kotlin and the receiver
DSL. Taikai remains a test-only dependency and is absent from the executable JAR.

To run via the Spring Boot Maven plugin, first install the reactor dependencies:

```sh
./mvnw install -DskipTests
./mvnw -pl examples/spring-kotlin spring-boot:run
```

The demo is built in CI but excluded from Maven Central publication. See
[the Kotlin manual](../../docs/kotlin.md) for all rules and compatibility details.
