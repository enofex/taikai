package com.enofex.taikai.exports;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.priority;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiException;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.java.JavaConfigurer;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.Priority;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SarifExportTest {

  private static final Path SOURCE_FILE =
      Path.of("src/test/java/com/enofex/taikai/exports/SarifExportTest.java");
  private static final Pattern RULE_ID = Pattern.compile("\"id\": \"(taikai-[0-9a-f]{12})\"");
  private static final Path CYCLE = Path.of("src/test/java/com/enofex/taikai/exports/cycle");

  @TempDir
  Path directory;

  @Test
  void shouldWriteViolationsWithTheirLocation() throws IOException {
    String sarif = export(Taikai.builder()
        .classes(Printer.class)
        .java(JavaConfigurer::noUsageOfSystemOutOrErr), SarifExport.file(sarifFile()));

    assertEquals("""
            {
              "$schema": "https://json.schemastore.org/sarif-2.1.0.json",
              "version": "2.1.0",
              "runs": [
                {
                  "tool": {
                    "driver": {
                      "name": "Taikai",
                      "informationUri": "https://enofex.github.io/taikai",
                      "rules": [
                        {
                          "id": "%1$s",
                          "shortDescription": {
                            "text": "Classes should not use System.out or System.err"
                          },
                          "fullDescription": {
                            "text": "Classes should not use System.out or System.err"
                          },
                          "help": {
                            "text": "Classes should not use System.out or System.err"
                          },
                          "defaultConfiguration": {
                            "level": "warning"
                          }
                        }
                      ]
                    }
                  },
                  "results": [
                    {
                      "ruleId": "%1$s",
                      "ruleIndex": 0,
                      "message": {
                        "text": "Method com.enofex.taikai.exports.SarifExportTest$Printer.print() calls java.lang.System.out"
                      },
                      "locations": [
                        {
                          "physicalLocation": {
                            "artifactLocation": {
                              "uri": "%2$s"
                            },
                            "region": {
                              "startLine": %3$d
                            }
                          }
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """.formatted(ruleId(sarif), SOURCE_FILE.toAbsolutePath().toUri(), printLine()),
        sarif);
  }

  @Test
  void shouldListRulesWithoutViolations() throws IOException {
    String sarif = export(Taikai.builder()
        .classes(Quiet.class)
        .java(JavaConfigurer::noUsageOfSystemOutOrErr), SarifExport.file(sarifFile()));

    assertTrue(sarif.contains("\"text\": \"Classes should not use System.out or System.err\""),
        sarif);
    assertTrue(sarif.contains("\"results\": []"), sarif);
  }

  @Test
  void shouldListRuleConfiguredTwiceOnce() throws IOException {
    TaikaiRule rule = TaikaiRule.of(noClasses().should().haveSimpleName("Printer"));

    String sarif = export(Taikai.builder()
        .classes(Printer.class)
        .addRule(rule)
        .addRule(rule), SarifExport.file(sarifFile()));

    assertEquals(1, occurrences(sarif, "\"shortDescription\""), sarif);
    assertEquals(1, occurrences(sarif, "\"ruleId\""), sarif);
  }

  @Test
  void shouldPointViolationsWithoutLineToFirstLine() throws IOException {
    String sarif = export(Taikai.builder()
        .classes(Printer.class)
        .addRule(TaikaiRule.of(noClasses().should().haveSimpleName("Printer"))),
        SarifExport.file(sarifFile()));

    assertTrue(sarif.contains("\"uri\": \"" + SOURCE_FILE.toAbsolutePath().toUri() + "\""),
        sarif);
    assertTrue(sarif.contains("\"startLine\": 1\n"), sarif);
  }

  @Test
  void shouldPointCyclesToTheirFirstDependency() throws IOException {
    String sarif = export(Taikai.builder()
        .classes(cycle())
        .addRule(TaikaiRule.of(slices().matching("com.enofex.taikai.exports.cycle.(*)..")
            .should().beFreeOfCycles())), SarifExport.file(sarifFile()));

    assertEquals(1, occurrences(sarif, "\"locations\""), sarif);
    assertTrue(sarif.contains(cycleUri("left/Left.java")), sarif);
  }

  @Test
  void shouldPointPackagesToTheirPackageInfoOrElseToTheirFirstClass() throws IOException {
    String sarif = export(Taikai.builder()
        .classes(cycle())
        .metrics(metrics -> metrics.packagesShouldNotExceedMaxClasses(0)),
        SarifExport.file(sarifFile()));

    assertEquals(2, occurrences(sarif, "\"locations\""), sarif);
    assertTrue(sarif.contains(cycleUri("left/Left.java")), sarif);
    assertTrue(sarif.contains(cycleUri("right/package-info.java")), sarif);
  }

  @Test
  void shouldMapPriorityToLevel() throws IOException {
    String sarif = export(Taikai.builder()
        .classes(Printer.class)
        .addRule(TaikaiRule.of(priority(Priority.HIGH).noClasses().should()
            .haveSimpleName("Printer")))
        .addRule(TaikaiRule.of(priority(Priority.LOW).noClasses().should()
            .haveSimpleName("Quiet"))), SarifExport.file(sarifFile()));

    assertTrue(sarif.contains("\"level\": \"error\""), sarif);
    assertTrue(sarif.contains("\"level\": \"note\""), sarif);
  }

  @Test
  void shouldFindSourceFileOutsideOfItsPackageDirectory() throws IOException {
    Path sourceFile = this.directory.resolve("sources/SarifExportTest.java");
    Files.createDirectories(sourceFile.getParent());
    Files.copy(SOURCE_FILE, sourceFile);

    String sarif = export(Taikai.builder()
            .classes(Printer.class)
            .java(JavaConfigurer::noUsageOfSystemOutOrErr),
        SarifExport.file(sarifFile())
            .withSourceDirectories(this.directory.resolve("sources").toString()));

    assertTrue(sarif.contains("\"uri\": \"" + sourceFile.toUri() + "\""), sarif);
  }

  @Test
  void shouldWriteViolationsWithoutLocationIfSourceFileIsMissing() throws IOException {
    String sarif = export(Taikai.builder()
            .classes(Printer.class)
            .java(JavaConfigurer::noUsageOfSystemOutOrErr),
        SarifExport.file(sarifFile())
            .withSourceDirectories(this.directory.resolve("missing").toString()));

    assertTrue(sarif.contains("calls java.lang.System.out"), sarif);
    assertFalse(sarif.contains("\"locations\""), sarif);
  }

  @Test
  void shouldEscapeJson() throws IOException {
    String sarif = export(Taikai.builder()
        .classes(Printer.class)
        .addRule(TaikaiRule.of(noClasses().should().haveSimpleName("Quiet")
            .because("of \"quotes\" and \\ backslashes"))), SarifExport.file(sarifFile()));

    assertTrue(sarif.contains("because of \\\"quotes\\\" and \\\\ backslashes\""), sarif);
  }

  @Test
  void shouldFailWithoutConfiguredFile() {
    Taikai taikai = Taikai.builder()
        .classes(Printer.class)
        .discoverExports(exports -> exports.exclude(AgentsMarkdownExport.class))
        .build();

    TaikaiException exception = assertThrows(TaikaiException.class, taikai::export);

    assertInstanceOf(IllegalStateException.class, exception.getCause());
    assertTrue(exception.getCause().getMessage().contains("add it with discoverExports("
        + "exports -> exports.configure(SarifExport.class, ...))"),
        exception.getCause().getMessage());
  }

  @Test
  void shouldRejectBlankFile() {
    assertThrows(IllegalArgumentException.class, () -> SarifExport.file(" "));
  }

  @Test
  void shouldRejectMissingSourceDirectories() {
    SarifExport.Settings settings = SarifExport.file("taikai.sarif");

    assertThrows(IllegalArgumentException.class, settings::withSourceDirectories);
  }

  private String sarifFile() {
    return this.directory.resolve("reports/taikai.sarif").toString();
  }

  private String export(Taikai.Builder builder, SarifExport.Settings settings) throws IOException {
    builder
        .discoverExports(exports -> exports
            .exclude(AgentsMarkdownExport.class)
            .configure(SarifExport.class, settings))
        .build()
        .export();

    return Files.readString(Path.of(settings.file()), StandardCharsets.UTF_8);
  }

  private static String ruleId(String sarif) {
    Matcher matcher = RULE_ID.matcher(sarif);
    assertTrue(matcher.find(), sarif);
    return matcher.group(1);
  }

  private static int printLine() throws IOException {
    List<String> lines = Files.readAllLines(SOURCE_FILE, StandardCharsets.UTF_8);

    return IntStream.range(0, lines.size())
        .filter(index -> lines.get(index).strip().equals("System.out.println(\"printed\");"))
        .findFirst()
        .orElseThrow() + 1;
  }

  private static long occurrences(String text, String part) {
    return Pattern.compile(Pattern.quote(part)).matcher(text).results().count();
  }

  /**
   * Imports the packages {@code left} and {@code right}, which depend on each other. Only
   * {@code right} has a {@code package-info}.
   */
  private static JavaClasses cycle() {
    return new ClassFileImporter().importPackages("com.enofex.taikai.exports.cycle");
  }

  private static String cycleUri(String file) {
    return "\"uri\": \"" + CYCLE.resolve(file).toAbsolutePath().toUri() + "\"";
  }

  static final class Printer {

    void print() {
      System.out.println("printed");
    }
  }

  static final class Quiet {

  }
}
