package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiException;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ImportsConfigurerTest {

  @Test
  void shouldImportByRegex() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ClassUsingAllowedImport.class))
        .java(java -> java.imports(
            imports -> imports.shouldImport(".*ClassUsingAllowedImport", "java\\.lang\\..*")))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowExceptionForImportByRegex() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ClassUsingAllowedImport.class))
        .java(java -> java.imports(
            imports -> imports.shouldImport(".*ClassUsingAllowedImport", "java\\.not\\.found..*")))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldNotImportSpecificPackage() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ClassUsingDisallowedImport.class))
        .java(java -> java.imports(imports -> imports.shouldNotImport("java.util")))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldNotImportByRegex() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ClassUsingDisallowedImport.class))
        .java(java -> java.imports(
            imports -> imports.shouldNotImport(".*ClassUsingDisallowedImport", "java\\.util\\..*")))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldThrowWhenNamespaceNotSetForCycles() {
    assertThrows(TaikaiException.class, () -> Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ClassUsingAllowedImport.class))
        .java(java -> java.imports(ImportsConfigurer::shouldHaveNoCycles)) // no namespace configured
        .build());
  }

  @Test
  void shouldAllowValidImports() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ClassUsingAllowedImport.class))
        .java(java -> java.imports(imports -> imports.shouldNotImport("java.sql")))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  static class ClassUsingAllowedImport {

    private final java.lang.String value = "ok";
  }

  static class ClassUsingDisallowedImport {

    private final java.util.List<String> list = java.util.Collections.emptyList();
  }
}
