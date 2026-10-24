package com.enofex.taikai.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    Files.writeString(this.directory.resolve("gradlew"), "#!/bin/sh");
    Files.writeString(this.directory.resolve("gradlew.bat"), "@echo off");
    Files.createDirectories(this.directory.resolve("gradle/wrapper"));
    Files.write(this.directory.resolve("gradle/wrapper/gradle-wrapper.jar"), JAR);
    Files.writeString(this.directory.resolve("gradle/wrapper/gradle-wrapper.properties"),
        "distributionUrl=https\\://services.gradle.org/distributions/gradle-bin.zip");

    assertEquals(List.of(), this.check.violations(context()));
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
}
