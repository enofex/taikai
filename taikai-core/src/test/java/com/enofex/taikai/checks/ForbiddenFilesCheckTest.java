package com.enofex.taikai.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ForbiddenFilesCheckTest {

  private final ForbiddenFilesCheck check = new ForbiddenFilesCheck();

  @TempDir
  private Path directory;

  @BeforeEach
  void setUp() throws IOException {
    create("src/main/resources/application.properties");
    create("src/main/java/com/example/Order.java");
    create("src/test/resources/keystore.p12");
    create(".env");
  }

  @Test
  void shouldDescribeCheck() {
    assertEquals("Project should not contain forbidden files", this.check.description());
  }

  @Test
  void shouldRequireConfiguration() {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> this.check.violations(context()));

    assertTrue(exception.getMessage().contains(
        "Check " + ForbiddenFilesCheck.class.getName() + " is not configured"),
        exception.getMessage());
  }

  @Test
  void shouldPassWithoutForbiddenFilesInConfiguredDirectory() {
    assertEquals(List.of(), this.check.violations(context().withConfiguration(
        ForbiddenFilesCheck.files("src/main/**/*.key", "src/main/**/*.p12", "src/main/**/.env"))));
  }

  @Test
  void shouldReportForbiddenFiles() throws IOException {
    create("src/main/resources/server.key");
    create("src/main/resources/config/.env");
    create("src/main/resources/heap.hprof");

    assertEquals(List.of(
            path("src/main/resources/config/.env") + " is forbidden (matches src/main/**/.env)",
            path("src/main/resources/heap.hprof") + " is forbidden (matches src/main/**/*.hprof)",
            path("src/main/resources/server.key") + " is forbidden (matches src/main/**/*.key)"),
        this.check.violations(context().withConfiguration(ForbiddenFilesCheck.files(
            "src/main/**/.env", "src/main/**/*.hprof", "src/main/**/*.key"))));
  }

  @Test
  void shouldReportConfiguredFiles() throws IOException {
    create("src/main/resources/server.key");
    create("deploy/secrets.txt");

    assertEquals(List.of(
            ".env is forbidden (matches **/.env)",
            path("deploy/secrets.txt") + " is forbidden (matches deploy/secrets.txt)"),
        this.check.violations(context()
            .withConfiguration(ForbiddenFilesCheck.files("**/.env", "deploy/secrets.txt"))));
  }

  @Test
  void shouldMatchPatternsWithoutWildcardsAndSingleSegmentWildcards() throws IOException {
    create("local.properties");
    create("config/dev/secret.txt");
    create("config/dev/nested/secret.txt");

    assertEquals(List.of(
            path("config/dev/secret.txt") + " is forbidden (matches config/*/secret.txt)",
            "local.properties is forbidden (matches local.properties)"),
        this.check.violations(context()
            .withConfiguration(ForbiddenFilesCheck.files("local.properties",
                "config/*/secret.txt"))));
  }

  @Test
  void shouldMatchNoDirectoryWithDoubleAsteriskSlash() throws IOException {
    create("src/main/server.key");
    create("a/b/c/d.txt");

    assertEquals(List.of(
            path("a/b/c/d.txt") + " is forbidden (matches a/**/b/**/d.txt)",
            path("src/main/server.key") + " is forbidden (matches src/main/**/*.key)"),
        this.check.violations(context().withConfiguration(
            ForbiddenFilesCheck.files("src/main/**/*.key", "a/**/b/**/d.txt"))));
  }

  @Test
  void shouldReportFileMatchingSeveralPatternsOnce() throws IOException {
    create("src/main/resources/server.key");

    assertEquals(List.of(path("src/main/resources/server.key")
            + " is forbidden (matches src/main/**/*.key)"),
        this.check.violations(context()
            .withConfiguration(ForbiddenFilesCheck.files("src/main/**/*.key", "**/server.*"))));
  }

  @Test
  void shouldReportForbiddenFilesOutsideOfProjectDirectory() throws IOException {
    Path module = Files.createDirectories(this.directory.resolve("module"));

    assertEquals(List.of(path("../.env") + " is forbidden (matches ../.env)"),
        this.check.violations(TestCheckContext.of(ForbiddenFilesCheck.class, module)
            .withConfiguration(ForbiddenFilesCheck.files("../.env", "../src/main/**/*.key"))));
  }

  @Test
  void shouldRejectSettingsWithoutFiles() {
    assertThrows(IllegalArgumentException.class, ForbiddenFilesCheck::files);
  }

  private TestCheckContext context() {
    return TestCheckContext.of(ForbiddenFilesCheck.class, this.directory);
  }

  private void create(String file) throws IOException {
    Path path = this.directory.resolve(file);
    Files.createDirectories(path.getParent());
    Files.writeString(path, "content");
  }

  private static String path(String file) {
    return Path.of(file).toString();
  }
}
