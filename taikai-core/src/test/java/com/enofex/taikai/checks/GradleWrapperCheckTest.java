package com.enofex.taikai.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GradleWrapperCheckTest {

  private static final byte[] JAR = {0x50, 0x4B, 0x03, 0x04, (byte) 0xFF, 0x00};

  private final GradleWrapperCheck check = new GradleWrapperCheck();

  @TempDir
  private Path directory;

  @Test
  void shouldDescribeCheck() {
    assertEquals("Project should have the Gradle Wrapper", this.check.description());
  }

  @Test
  void shouldPassWithGradleWrapper() throws IOException {
    writeGradleWrapper(this.directory);

    assertEquals(List.of(), this.check.violations(context()));
  }

  @Test
  void shouldPassWithGradleWrapperInConfiguredDirectory() throws IOException {
    Path subproject = Files.createDirectories(this.directory.resolve("subproject"));
    writeGradleWrapper(this.directory);

    assertEquals(List.of(), this.check.violations(TestCheckContext
        .of(GradleWrapperCheck.class, subproject)
        .withConfiguration(GradleWrapperCheck.directories(".."))));
  }

  @Test
  void shouldReportMissingGradleWrapperFilesInConfiguredDirectory() throws IOException {
    Path subproject = Files.createDirectories(this.directory.resolve("subproject"));
    writeGradleWrapper(subproject);

    assertEquals(List.of(
            "gradlew is missing in " + this.directory,
            "gradlew.bat is missing in " + this.directory,
            "gradle/wrapper/gradle-wrapper.jar is missing in " + this.directory,
            "gradle/wrapper/gradle-wrapper.properties is missing in " + this.directory),
        this.check.violations(TestCheckContext
            .of(GradleWrapperCheck.class, subproject)
            .withConfiguration(GradleWrapperCheck.directories(".."))));
  }

  @Test
  void shouldCheckEveryConfiguredDirectory() throws IOException {
    Path subproject = Files.createDirectories(this.directory.resolve("subproject"));
    Path tools = Files.createDirectories(this.directory.resolve("tools"));
    writeGradleWrapper(this.directory);

    assertEquals(List.of(
            "gradlew is missing in " + tools,
            "gradlew.bat is missing in " + tools,
            "gradle/wrapper/gradle-wrapper.jar is missing in " + tools,
            "gradle/wrapper/gradle-wrapper.properties is missing in " + tools),
        this.check.violations(TestCheckContext
            .of(GradleWrapperCheck.class, subproject)
            .withConfiguration(GradleWrapperCheck.directories("..", "../tools"))));
  }

  @Test
  void shouldRejectSettingsWithoutDirectories() {
    assertThrows(IllegalArgumentException.class, GradleWrapperCheck::directories);
  }

  @Test
  void shouldReportMissingGradleWrapperFiles() {
    assertEquals(List.of(
            "gradlew is missing in " + this.directory,
            "gradlew.bat is missing in " + this.directory,
            "gradle/wrapper/gradle-wrapper.jar is missing in " + this.directory,
            "gradle/wrapper/gradle-wrapper.properties is missing in " + this.directory),
        this.check.violations(context()));
  }

  @Test
  void shouldReportEmptyGradleWrapperFiles() throws IOException {
    Files.writeString(this.directory.resolve("gradlew"), "#!/bin/sh");
    Path script = Files.writeString(this.directory.resolve("gradlew.bat"), "");
    Files.createDirectories(this.directory.resolve("gradle/wrapper"));
    Path jar = Files.write(this.directory.resolve("gradle/wrapper/gradle-wrapper.jar"),
        new byte[0]);
    Files.writeString(this.directory.resolve("gradle/wrapper/gradle-wrapper.properties"),
        "distributionUrl=https\\://services.gradle.org/distributions/gradle-bin.zip");

    assertEquals(List.of(script + " is empty", jar + " is empty"),
        this.check.violations(context()));
  }

  private TestCheckContext context() {
    return TestCheckContext.of(GradleWrapperCheck.class, this.directory);
  }

  private static void writeGradleWrapper(Path directory) throws IOException {
    Files.writeString(directory.resolve("gradlew"), "#!/bin/sh");
    Files.writeString(directory.resolve("gradlew.bat"), "@echo off");
    Files.createDirectories(directory.resolve("gradle/wrapper"));
    Files.write(directory.resolve("gradle/wrapper/gradle-wrapper.jar"), JAR);
    Files.writeString(directory.resolve("gradle/wrapper/gradle-wrapper.properties"),
        "distributionUrl=https\\://services.gradle.org/distributions/gradle-bin.zip");
  }
}
