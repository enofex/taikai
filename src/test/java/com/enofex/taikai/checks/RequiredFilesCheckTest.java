package com.enofex.taikai.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RequiredFilesCheckTest {

  private final RequiredFilesCheck check = new RequiredFilesCheck();

  @TempDir
  private Path directory;

  @Test
  void shouldDescribeCheck() {
    assertEquals("Project should have the required files", this.check.description());
  }

  @Test
  void shouldRequireConfiguration() {
    TestCheckContext context = TestCheckContext.of(RequiredFilesCheck.class, this.directory);

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> this.check.violations(context));

    assertTrue(exception.getMessage().contains(
        "Check " + RequiredFilesCheck.class.getName() + " is not configured"),
        exception.getMessage());
  }

  @Test
  void shouldPassWhenRequiredFilesHaveContent() throws IOException {
    Files.writeString(this.directory.resolve("README.md"), "# Project");

    assertEquals(List.of(), this.check.violations(readme()));
  }

  @Test
  void shouldRequireConfiguredFiles() throws IOException {
    Files.writeString(this.directory.resolve("LICENSE"), "MIT License");
    Files.createDirectories(this.directory.resolve(".github"));
    Files.writeString(this.directory.resolve(".github/CODEOWNERS"), "* @team");

    TestCheckContext context = TestCheckContext.of(RequiredFilesCheck.class, this.directory)
        .withConfiguration(
            RequiredFilesCheck.files("LICENSE", ".github/CODEOWNERS", "SECURITY.md"));

    assertEquals(List.of("SECURITY.md is missing in " + this.directory),
        this.check.violations(context));
  }

  @Test
  void shouldReportDirectoryInsteadOfFileAsMissing() throws IOException {
    Files.createDirectory(this.directory.resolve("README.md"));

    assertEquals(List.of("README.md is missing in " + this.directory),
        this.check.violations(readme()));
  }

  @Test
  void shouldReportEmptyFile() throws IOException {
    Path readme = Files.writeString(this.directory.resolve("README.md"), " \n\t\n");

    assertEquals(List.of(readme + " is empty"),
        this.check.violations(readme()));
  }

  @Test
  void shouldRejectSettingsWithoutFiles() {
    assertThrows(IllegalArgumentException.class, RequiredFilesCheck::files);
  }

  private TestCheckContext readme() {
    return TestCheckContext.of(RequiredFilesCheck.class, this.directory)
        .withConfiguration(RequiredFilesCheck.files("README.md"));
  }
}
