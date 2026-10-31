package com.enofex.taikai.exports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiException;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.java.JavaConfigurer;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AgentsMarkdownExportTest {

  @TempDir
  Path directory;

  @Test
  void shouldWriteDescriptionsOfRules() throws IOException {
    Path file = this.directory.resolve("ARCHITECTURE.md");

    Taikai.builder()
        .namespace("com.company.project")
        .java(java -> java
            .noUsageOfSystemOutOrErr()
            .fieldsShouldNotBePublic())
        .discoverExports(exports -> exports
            .exclude(SarifExport.class)
            .configure(AgentsMarkdownExport.class, AgentsMarkdownExport.file(file.toString())))
        .build()
        .export();

    assertEquals("""
            # Architecture Rules

            The architecture test enforces these rules for `com.company.project`, so code that \
            breaks one of them fails the build.

            - Classes should not use System.out or System.err
            - Fields should not be public unless they are static
            """,
        Files.readString(file, StandardCharsets.UTF_8));
  }

  @Test
  void shouldNotNameNamespaceForDirectlySuppliedClasses() throws IOException {
    Path file = this.directory.resolve("ARCHITECTURE.md");

    Taikai.builder()
        .classes(AgentsMarkdownExportTest.class)
        .java(JavaConfigurer::noUsageOfSystemOutOrErr)
        .discoverExports(exports -> exports
            .exclude(SarifExport.class)
            .configure(AgentsMarkdownExport.class, AgentsMarkdownExport.file(file.toString())))
        .build()
        .export();

    assertEquals("""
            # Architecture Rules

            The architecture test enforces these rules, so code that breaks one of them fails \
            the build.

            - Classes should not use System.out or System.err
            """,
        Files.readString(file, StandardCharsets.UTF_8));
  }

  @Test
  void shouldListRuleConfiguredTwiceOnce() throws IOException {
    Path file = this.directory.resolve("ARCHITECTURE.md");
    TaikaiRule rule = TaikaiRule.of(ArchRuleDefinition.classes()
        .should().haveSimpleNameNotEndingWith("Impl"));

    Taikai.builder()
        .namespace("com.company.project")
        .addRule(rule)
        .addRule(rule)
        .discoverExports(exports -> exports
            .exclude(SarifExport.class)
            .configure(AgentsMarkdownExport.class, AgentsMarkdownExport.file(file.toString())))
        .build()
        .export();

    assertEquals(1, Files.readString(file, StandardCharsets.UTF_8)
        .lines()
        .filter(line -> line.startsWith("- "))
        .count());
  }

  @Test
  void shouldIndentFurtherLinesOfMultiLineDescriptions() throws IOException {
    Path file = this.directory.resolve("ARCHITECTURE.md");

    Taikai.builder()
        .namespace("com.company.project")
        .addRule(TaikaiRule.of(ArchRuleDefinition.classes()
            .should().haveSimpleNameNotEndingWith("Impl")
            .as("Layered architecture, consisting of\nlayer 'Api'\r\nlayer 'Domain'")))
        .discoverExports(exports -> exports
            .exclude(SarifExport.class)
            .configure(AgentsMarkdownExport.class, AgentsMarkdownExport.file(file.toString())))
        .build()
        .export();

    assertEquals("""
            # Architecture Rules

            The architecture test enforces these rules for `com.company.project`, so code that \
            breaks one of them fails the build.

            - Layered architecture, consisting of
              layer 'Api'
              layer 'Domain'
            """,
        Files.readString(file, StandardCharsets.UTF_8));
  }

  @Test
  void shouldReplaceFileAndCreateMissingDirectories() throws IOException {
    Path file = this.directory.resolve("docs/agents/ARCHITECTURE.md");
    Files.createDirectories(file.getParent());
    Files.writeString(file, "old content that is longer than the new one".repeat(10));

    Taikai.builder()
        .namespace("com.company.project")
        .discoverExports(exports -> exports
            .exclude(SarifExport.class)
            .configure(AgentsMarkdownExport.class, AgentsMarkdownExport.file(file.toString())))
        .build()
        .export();

    assertTrue(Files.readString(file, StandardCharsets.UTF_8).startsWith("# Architecture Rules"));
    assertFalse(Files.readString(file, StandardCharsets.UTF_8).contains("old content"));
  }

  @Test
  void shouldFailWithoutConfiguredFile() {
    Taikai taikai = Taikai.builder()
        .namespace("com.company.project")
        .discoverExports(exports -> exports.exclude(SarifExport.class))
        .build();

    TaikaiException exception = assertThrows(TaikaiException.class, taikai::export);

    assertInstanceOf(IllegalStateException.class, exception.getCause());
    assertTrue(exception.getCause().getMessage().contains("add it with discoverExports("
        + "exports -> exports.configure(AgentsMarkdownExport.class, ...))"),
        exception.getCause().getMessage());
  }

  @Test
  void shouldRejectBlankFile() {
    assertThrows(IllegalArgumentException.class, () -> AgentsMarkdownExport.file(" "));
  }
}
