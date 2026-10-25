package com.enofex.taikai;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NamespaceTest {

  private static final String VALID_NAMESPACE = "com.enofex.taikai";

  @Test
  void shouldReturnJavaClassesWithoutTests() {
    JavaClasses result = Namespace.from(VALID_NAMESPACE, Namespace.IMPORT.WITHOUT_TESTS);

    assertNotNull(result);
    assertDoesNotThrow(() -> new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .withImportOption(new ImportOption.DoNotIncludeJars())
        .importPackages(VALID_NAMESPACE));
  }

  @Test
  void shouldReturnJavaClassesWithTests() {
    JavaClasses result = Namespace.from(VALID_NAMESPACE, Namespace.IMPORT.WITH_TESTS);

    assertNotNull(result);
    assertDoesNotThrow(() -> new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeJars())
        .importPackages(VALID_NAMESPACE));
  }

  @Test
  void shouldReturnJavaClassesOnlyTests() {
    JavaClasses result = Namespace.from(VALID_NAMESPACE, Namespace.IMPORT.ONLY_TESTS);

    assertNotNull(result);
    assertDoesNotThrow(() -> new ClassFileImporter()
        .withImportOption(new ImportOption.OnlyIncludeTests())
        .withImportOption(new ImportOption.DoNotIncludeJars())
        .importPackages(VALID_NAMESPACE));
  }

  @Test
  void shouldThrowExceptionForNullNamespace() {
    assertThrows(NullPointerException.class,
        () -> Namespace.from(null, Namespace.IMPORT.WITHOUT_TESTS));
  }

  @Test
  void shouldThrowExceptionForNullImportOption() {
    assertThrows(NullPointerException.class, () -> Namespace.from(VALID_NAMESPACE, null));
  }
}
