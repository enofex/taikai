package com.enofex.taikai.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MavenWrapperCheckTest {

  private final MavenWrapperCheck check = new MavenWrapperCheck();

  @TempDir
  private Path directory;

  @Test
  void shouldDescribeCheck() {
    assertEquals("Project should have the Maven Wrapper", this.check.description());
  }

  @Test
  void shouldPassWithMavenWrapper() throws IOException {
    writeMavenWrapper(this.directory);

    assertEquals(List.of(), this.check.violations(context()));
  }

  @Test
  void shouldPassWithMavenWrapperInConfiguredDirectory() throws IOException {
    Path module = Files.createDirectories(this.directory.resolve("module"));
    writeMavenWrapper(this.directory);

    assertEquals(List.of(), this.check.violations(TestCheckContext
        .of(MavenWrapperCheck.class, module)
        .withConfiguration(MavenWrapperCheck.directory(".."))));
  }

  @Test
  void shouldReportMissingMavenWrapperFilesInConfiguredDirectory() throws IOException {
    Path module = Files.createDirectories(this.directory.resolve("module"));
    writeMavenWrapper(module);

    assertEquals(List.of(
            "mvnw is missing in " + this.directory,
            "mvnw.cmd is missing in " + this.directory,
            ".mvn/wrapper/maven-wrapper.properties is missing in " + this.directory),
        this.check.violations(TestCheckContext
            .of(MavenWrapperCheck.class, module)
            .withConfiguration(MavenWrapperCheck.directory(".."))));
  }

  @Test
  void shouldReportMissingMavenWrapperFiles() {
    assertEquals(List.of(
            "mvnw is missing in " + this.directory,
            "mvnw.cmd is missing in " + this.directory,
            ".mvn/wrapper/maven-wrapper.properties is missing in " + this.directory),
        this.check.violations(context()));
  }

  @Test
  void shouldReportEmptyMavenWrapperFile() throws IOException {
    Files.writeString(this.directory.resolve("mvnw"), "#!/bin/sh");
    Path script = Files.writeString(this.directory.resolve("mvnw.cmd"), "");
    Files.createDirectories(this.directory.resolve(".mvn/wrapper"));
    Files.writeString(this.directory.resolve(".mvn/wrapper/maven-wrapper.properties"),
        "distributionUrl=https://repo.maven.apache.org/maven2");

    assertEquals(List.of(script + " is empty"), this.check.violations(context()));
  }

  private TestCheckContext context() {
    return TestCheckContext.of(MavenWrapperCheck.class, this.directory);
  }

  private static void writeMavenWrapper(Path directory) throws IOException {
    Files.writeString(directory.resolve("mvnw"), "#!/bin/sh");
    Files.writeString(directory.resolve("mvnw.cmd"), "@echo off");
    Files.createDirectories(directory.resolve(".mvn/wrapper"));
    Files.writeString(directory.resolve(".mvn/wrapper/maven-wrapper.properties"),
        "distributionUrl=https://repo.maven.apache.org/maven2");
  }
}
