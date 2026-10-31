package com.enofex.taikai.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileContentCheckTest {

  private final FileContentCheck check = new FileContentCheck();

  @TempDir
  private Path directory;

  @BeforeEach
  void setUp() throws IOException {
    create("src/main/java/com/example/Order.java", """
        package com.example;

        import java.util.List;

        class Order {
        }
        """);
    create("src/main/java/com/example/package-info.java", """
        @NullMarked
        package com.example;
        """);
  }

  @Test
  void shouldDescribeCheck() {
    assertEquals("Project files should follow the content rules", this.check.description());
  }

  @Test
  void shouldRequireConfiguration() {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> this.check.violations(context()));

    assertTrue(exception.getMessage().contains(
        "Check " + FileContentCheck.class.getName() + " is not configured"),
        exception.getMessage());
  }

  @Test
  void shouldPassWhenContentFollowsRules() {
    assertEquals(List.of(), this.check.violations(context().withConfiguration(
        FileContentCheck.rules(
            FileContentCheck.forbid("src/main/**/*.java", "^import .*\\*;"),
            FileContentCheck.require("src/main/**/package-info.java", "@NullMarked")))));
  }

  @Test
  void shouldReportEveryLineWithForbiddenContent() throws IOException {
    create("src/main/java/com/example/Invoice.java", """
        package com.example;

        import java.util.*;
        import java.io.*;

        class Invoice {
        }
        """);

    assertEquals(List.of(
            path("src/main/java/com/example/Invoice.java")
                + ":3 contains forbidden content (matches ^import .*\\*;)",
            path("src/main/java/com/example/Invoice.java")
                + ":4 contains forbidden content (matches ^import .*\\*;)"),
        this.check.violations(context().withConfiguration(FileContentCheck.rules(
            FileContentCheck.forbid("src/main/**/*.java", "^import .*\\*;")))));
  }

  @Test
  void shouldReportLineWithSeveralMatchesOnce() throws IOException {
    create("src/main/resources/notes.txt", "first\nTODO and TODO\nlast TODO");

    assertEquals(List.of(
            path("src/main/resources/notes.txt") + ":2 contains forbidden content (matches TODO)",
            path("src/main/resources/notes.txt") + ":3 contains forbidden content (matches TODO)"),
        this.check.violations(context().withConfiguration(FileContentCheck.rules(
            FileContentCheck.forbid("src/main/resources/*.txt", "TODO")))));
  }

  @Test
  void shouldReportForbiddenContentAcrossLinesAtItsFirstLine() throws IOException {
    create("src/main/resources/application.properties", "a=1\nb=2\nc=3\n");

    assertEquals(List.of(path("src/main/resources/application.properties")
            + ":2 contains forbidden content (matches b=2\\nc=3)"),
        this.check.violations(context().withConfiguration(FileContentCheck.rules(
            FileContentCheck.forbid("src/main/resources/*.properties", "b=2\\nc=3")))));
  }

  @Test
  void shouldMatchLinesWithWindowsLineEndings() throws IOException {
    create("src/main/resources/application.properties", "a=1\r\nb=2\r\npassword=x\r\n");

    assertEquals(List.of(path("src/main/resources/application.properties")
            + ":3 contains forbidden content (matches ^password=x$)"),
        this.check.violations(context().withConfiguration(FileContentCheck.rules(
            FileContentCheck.forbid("src/main/resources/*.properties", "^password=x$"),
            FileContentCheck.require("src/main/resources/*.properties", "^a=1$")))));
  }

  @Test
  void shouldIgnoreByteOrderMark() throws IOException {
    create("src/main/resources/application.properties", "\uFEFFpassword=x\n");

    assertEquals(List.of(path("src/main/resources/application.properties")
            + ":1 contains forbidden content (matches ^password=)"),
        this.check.violations(context().withConfiguration(FileContentCheck.rules(
            FileContentCheck.forbid("src/main/resources/*.properties", "^password=")))));
  }

  @Test
  void shouldReportFilesWithoutRequiredContent() throws IOException {
    create("src/main/java/com/example/billing/package-info.java", """
        package com.example.billing;
        """);

    assertEquals(List.of(path("src/main/java/com/example/billing/package-info.java")
            + " does not contain required content (should match @NullMarked)"),
        this.check.violations(context().withConfiguration(FileContentCheck.rules(
            FileContentCheck.require("src/main/**/package-info.java", "@NullMarked")))));
  }

  @Test
  void shouldMatchStartOfFileWithBackslashA() {
    assertEquals(List.of(path("src/main/java/com/example/package-info.java")
            + " does not contain required content (should match \\Apackage )"),
        this.check.violations(context().withConfiguration(FileContentCheck.rules(
            FileContentCheck.require("src/main/**/*.java", "\\Apackage ")))));
  }

  @Test
  void shouldApplyCustomCondition() {
    assertEquals(List.of(path("src/main/java/com/example/Order.java") + " has 6 lines"),
        this.check.violations(context().withConfiguration(FileContentCheck.rules(
            FileContentCheck.rule("src/main/**/*.java", (file, content) ->
                content.lines().count() > 3
                    ? List.of(file + " has " + content.lines().count() + " lines")
                    : List.of())))));
  }

  @Test
  void shouldReportViolationsSortedByFileInOrderOfRules() throws IOException {
    create("src/main/java/com/example/Customer.java", "import java.util.*;\n");

    assertEquals(List.of(
            path("src/main/java/com/example/Customer.java")
                + ":1 contains forbidden content (matches ^import .*\\*;)",
            path("src/main/java/com/example/Customer.java")
                + " does not contain required content (should match ^class )",
            path("src/main/java/com/example/package-info.java")
                + " does not contain required content (should match ^class )"),
        this.check.violations(context().withConfiguration(FileContentCheck.rules(
            FileContentCheck.forbid("src/main/**/*.java", "^import .*\\*;"),
            FileContentCheck.require("src/main/**/*.java", "^class ")))));
  }

  @Test
  void shouldOnlyCheckFilesMatchingRule() throws IOException {
    create("src/test/java/com/example/OrderTest.java", "import java.util.*;\n");
    create("target/classes/notes.txt", "import java.util.*;\n");

    assertEquals(List.of(), this.check.violations(context().withConfiguration(
        FileContentCheck.rules(FileContentCheck.forbid("src/main/**/*.java", "^import .*\\*;")))));
  }

  @Test
  void shouldSkipBinaryFiles() throws IOException {
    create("src/main/resources/keystore.p12", new byte[] {'s', 'e', 'c', 'r', 'e', 't', 0, 1, 2});

    assertEquals(List.of(), this.check.violations(context().withConfiguration(
        FileContentCheck.rules(FileContentCheck.forbid("src/main/resources/**", "secret")))));
  }

  @Test
  void shouldReadFilesThatAreNotUtf8() throws IOException {
    create("src/main/resources/messages.properties",
        "greeting=Grüße\npassword=secret\n".getBytes(StandardCharsets.ISO_8859_1));

    assertEquals(List.of(path("src/main/resources/messages.properties")
            + ":2 contains forbidden content (matches ^password=)"),
        this.check.violations(context().withConfiguration(FileContentCheck.rules(
            FileContentCheck.forbid("src/main/resources/*.properties", "^password=")))));
  }

  @Test
  void shouldCheckFilesOutsideOfProjectDirectory() throws IOException {
    Path module = Files.createDirectories(this.directory.resolve("module"));
    create("README.md", "# Project\n");

    assertEquals(List.of(path("../README.md")
            + " does not contain required content (should match ^## License)"),
        this.check.violations(TestCheckContext.of(FileContentCheck.class, module)
            .withConfiguration(FileContentCheck.rules(
                FileContentCheck.require("../README.md", "^## License")))));
  }

  @Test
  void shouldRejectInvalidRegularExpression() {
    assertThrows(PatternSyntaxException.class,
        () -> FileContentCheck.forbid("src/main/**/*.java", "import ("));
    assertThrows(PatternSyntaxException.class,
        () -> FileContentCheck.require("src/main/**/*.java", "import ("));
  }

  @Test
  void shouldRejectSettingsWithoutRules() {
    assertThrows(IllegalArgumentException.class, FileContentCheck::rules);
  }

  private TestCheckContext context() {
    return TestCheckContext.of(FileContentCheck.class, this.directory);
  }

  private void create(String file, String content) throws IOException {
    create(file, content.getBytes(StandardCharsets.UTF_8));
  }

  private void create(String file, byte[] content) throws IOException {
    Path path = this.directory.resolve(file);
    Files.createDirectories(path.getParent());
    Files.write(path, content);
  }

  private static String path(String file) {
    return Path.of(file).toString();
  }
}
