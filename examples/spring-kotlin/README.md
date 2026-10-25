# Spring Kotlin demo

A small Spring Boot task API whose architecture is checked by Taikai. All rules and the
predefined project checks are configured with the receiver DSL in
[`ArchitectureTest`](src/test/kotlin/com/enofex/taikai/demo/ArchitectureTest.kt) and
reported as one test each. The code is packaged by feature: everything in `task` is
`internal`, Kotlin's closest equivalent to package-private.

From the repository root, with JDK 17 or later:

```sh
./mvnw -pl examples/spring-kotlin -am verify
java -jar examples/spring-kotlin/target/taikai-spring-kotlin-demo-*.jar

curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{"title":"Learn Taikai"}'
curl http://localhost:8080/tasks
```

See the [Kotlin manual](../../docs/kotlin.md) for all rules and compatibility details, and
the [Spring Java demo](../spring-java) for the same application in Java.
