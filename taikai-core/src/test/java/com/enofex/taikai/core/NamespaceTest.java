package com.enofex.taikai.core;

import static com.enofex.taikai.core.Namespace.Import.ONLY_TESTS;
import static com.enofex.taikai.core.Namespace.Import.WITHOUT_TESTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class NamespaceTest {

  private static final String VALID_NAMESPACE = "com.enofex.taikai";

  @ParameterizedTest(name = "{0}")
  @EnumSource(Namespace.Import.class)
  void shouldImportClassesOfImportModeOnce(Namespace.Import importOption) {
    JavaClasses result = Namespace.from(VALID_NAMESPACE, importOption);

    assertEquals(importOption != ONLY_TESTS, result.contain(Taikai.class), "production class");
    assertEquals(importOption != WITHOUT_TESTS, result.contain(NamespaceTest.class), "test class");
    assertSame(result, Namespace.from(VALID_NAMESPACE, importOption));
  }

  @Test
  void shouldThrowExceptionForNullNamespace() {
    assertThrows(NullPointerException.class, () -> Namespace.from(null, WITHOUT_TESTS));
  }

  @Test
  void shouldThrowExceptionForNullImportOption() {
    assertThrows(NullPointerException.class, () -> Namespace.from(VALID_NAMESPACE, null));
  }
}
