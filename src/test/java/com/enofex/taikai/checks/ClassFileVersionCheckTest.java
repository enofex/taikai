package com.enofex.taikai.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassFileVersionCheckTest {

  private final ClassFileVersionCheck check = new ClassFileVersionCheck();

  @Test
  void shouldDescribeCheck() {
    assertEquals("Classes should not be compiled for a newer Java version than configured",
        this.check.description());
  }

  @Test
  void shouldPassForClassesCompiledForAllowedJavaVersion() {
    int runtimeJavaVersion = Runtime.version().feature();

    assertEquals(List.of(), this.check.violations(
        context(ClassFileVersionCheck.javaVersion(runtimeJavaVersion))));
  }

  @Test
  void shouldReportClassesCompiledForNewerJavaVersionSortedByName() {
    List<String> violations = this.check.violations(context(ClassFileVersionCheck.javaVersion(1)));

    assertEquals(2, violations.size());
    assertTrue(violations.get(0).startsWith(
        "Class " + ClassFileVersionCheck.class.getName() + " is compiled for Java "),
        violations.get(0));
    assertTrue(violations.get(0).endsWith(", but at most Java 1 is allowed"), violations.get(0));
    assertTrue(violations.get(1).startsWith("Class " + RequiredFilesCheck.class.getName()),
        violations.get(1));
  }

  @Test
  void shouldRequireConfiguration() {
    TestCheckContext context = TestCheckContext.of(ClassFileVersionCheck.class, Path.of(""))
        .withClasses(new ClassFileImporter().importClasses(ClassFileVersionCheck.class));

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> this.check.violations(context));

    assertTrue(exception.getMessage().contains(
        "Check " + ClassFileVersionCheck.class.getName() + " is not configured"),
        exception.getMessage());
  }

  @Test
  void shouldRejectJavaVersionThatIsNotPositive() {
    assertThrows(IllegalArgumentException.class, () -> ClassFileVersionCheck.javaVersion(0));
  }

  private static TestCheckContext context(ClassFileVersionCheck.Settings settings) {
    return TestCheckContext.of(ClassFileVersionCheck.class, Path.of(""))
        .withClasses(new ClassFileImporter()
            .importClasses(RequiredFilesCheck.class, ClassFileVersionCheck.class))
        .withConfiguration(settings);
  }
}
