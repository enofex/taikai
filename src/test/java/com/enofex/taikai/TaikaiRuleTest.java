package com.enofex.taikai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.enofex.taikai.TaikaiRule.Configuration;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class TaikaiRuleTest {

  @Test
  void shouldConstructWithArchRuleAndDefaultConfiguration() {
    ArchRule archRule = mock(ArchRule.class);
    TaikaiRule taikaiRule = TaikaiRule.of(archRule);

    assertNotNull(taikaiRule);
    assertEquals(archRule, taikaiRule.archRule());
    assertNotNull(taikaiRule.configuration());
  }

  @Test
  void shouldConstructWithArchRuleAndGivenConfiguration() {
    ArchRule archRule = mock(ArchRule.class);
    Configuration configuration = Configuration.of("com.example");
    TaikaiRule taikaiRule = TaikaiRule.of(archRule, configuration);

    assertNotNull(taikaiRule);
    assertEquals(archRule, taikaiRule.archRule());
    assertSame(configuration, taikaiRule.configuration());
  }

  @Test
  void shouldThrowNullPointerExceptionWhenConstructedWithNullArchRule() {
    assertThrows(NullPointerException.class, () -> TaikaiRule.of(null));
  }

  @Test
  void shouldReturnConfigurationNamespace() {
    Configuration configuration = Configuration.of("com.example");

    assertEquals("com.example", configuration.namespace());
  }

  @Test
  void shouldReturnConfigurationNamespaceImport() {
    Configuration configuration = Configuration.of(Namespace.IMPORT.ONLY_TESTS);

    assertEquals(Namespace.IMPORT.ONLY_TESTS, configuration.namespaceImport());
  }

  @Test
  void shouldReturnConfigurationJavaClasses() {
    JavaClasses javaClasses = mock(JavaClasses.class);
    Configuration configuration = Configuration.of(javaClasses);

    assertEquals(javaClasses, configuration.javaClasses());
  }

  @Test
  void shouldCheckUsingGivenJavaClasses() {
    JavaClasses javaClasses = mock(JavaClasses.class);
    ArchRule archRule = mock(ArchRule.class);
    TaikaiRule.of(archRule, Configuration.of(javaClasses)).check(null);

    verify(archRule).check(javaClasses);
  }

  @Test
  void shouldCheckUsingNamespaceFromConfiguration() {
    ArchRule archRule = mock(ArchRule.class);
    TaikaiRule.of(archRule, Configuration.of("com.example", Namespace.IMPORT.WITH_TESTS))
        .check(null);

    verify(archRule).check(Namespace.from("com.example", Namespace.IMPORT.WITH_TESTS));
  }

  @Test
  void shouldCheckUsingGlobalNamespace() {
    ArchRule archRule = mock(ArchRule.class);
    TaikaiRule.of(archRule).check("com.example");

    verify(archRule).check(Namespace.from("com.example", Namespace.IMPORT.WITHOUT_TESTS));
  }

  @Test
  void shouldThrowTaikaiExceptionWhenNoNamespaceProvided() {
    ArchRule archRule = mock(ArchRule.class);
    TaikaiRule taikaiRule = TaikaiRule.of(archRule, Configuration.defaultConfiguration());

    assertThrows(TaikaiException.class, () -> taikaiRule.check(null));
  }
}
