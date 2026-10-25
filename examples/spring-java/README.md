# Spring Java demo

A small Spring Boot task API whose architecture is checked by Taikai. All rules and the
predefined project checks are configured in
[`ArchitectureTest`](src/test/java/com/enofex/taikai/demo/ArchitectureTest.java) and
reported as one test each. The code is packaged by feature: everything in `task` is
package-private, and only `TaskApplication` is public.

From the repository root, with JDK 17 or later:

```sh
./mvnw -pl examples/spring-java -am verify
java -jar examples/spring-java/target/taikai-spring-java-demo-*.jar

curl -X POST http://localhost:8080/tasks -H 'Content-Type: application/json' -d '{"title":"Learn Taikai"}'
curl http://localhost:8080/tasks
```

See the [documentation](../../docs/documentation.md) for all rules and checks, and the
[Spring Kotlin demo](../spring-kotlin) for the same application in Kotlin.
