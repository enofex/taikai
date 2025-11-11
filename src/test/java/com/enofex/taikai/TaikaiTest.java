
package com.enofex.taikai;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.java.JavaConfigurer;
import com.enofex.taikai.spring.SpringConfigurer;
import com.enofex.taikai.test.TestConfigurer;
import com.tngtech.archunit.ArchConfiguration;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaikaiTest {

  private static final String VALID_NAMESPACE = "com.enofex.taikai";

  @BeforeEach
  void setUp() {
    ArchConfiguration.get().reset();
  }

  @Test
  void shouldBuildTaikaiWithDefaultValues() {
    Taikai taikai = Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .build();

    assertFalse(taikai.failOnEmpty());
    assertEquals(VALID_NAMESPACE, taikai.namespace());
    assertNull(taikai.classes());
    assertTrue(taikai.rules().isEmpty());
    assertTrue(taikai.excludedClasses().isEmpty());
  }

  @Test
  void shouldBuildTaikaiWithCustomValues() {
    TaikaiRule mockRule = mock(TaikaiRule.class);
    Collection<TaikaiRule> rules = Collections.singletonList(mockRule);

    Taikai taikai = Taikai.builder()
        .classes(TaikaiTest.class)
        .excludeClasses("com.enofex.taikai.foo.ClassToExclude", "com.enofex.taikai.bar.ClassToExclude")
        .failOnEmpty(true)
        .addRules(rules)
        .build();

    assertTrue(taikai.failOnEmpty());
    assertNull(taikai.namespace());
    assertNotNull(taikai.classes());
    assertEquals(1, taikai.rules().size());
    assertTrue(taikai.rules().contains(mockRule));
    assertEquals(2, taikai.excludedClasses().size());
  }

  @Test
  void shouldAddSingleRule() {
    TaikaiRule mockRule = mock(TaikaiRule.class);

    Taikai taikai = Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .addRule(mockRule)
        .build();

    assertEquals(1, taikai.rules().size());
    assertTrue(taikai.rules().contains(mockRule));
  }

  @Test
  void shouldConfigureJavaCustomizer() {
    Customizer<JavaConfigurer> customizer = mock(Customizer.class);

    Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .java(customizer)
        .build();

    verify(customizer, times(1)).customize(any(JavaConfigurer.class));
  }

  @Test
  void shouldConfigureSpringCustomizer() {
    Customizer<SpringConfigurer> customizer = mock(Customizer.class);

    Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .spring(customizer)
        .build();

    verify(customizer, times(1)).customize(any(SpringConfigurer.class));
  }

  @Test
  void shouldConfigureTestCustomizer() {
    Customizer<TestConfigurer> customizer = mock(Customizer.class);

    Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .test(customizer)
        .build();

    verify(customizer, times(1)).customize(any(TestConfigurer.class));
  }

  @Test
  void shouldThrowExceptionForNullCustomizer() {
    assertThrows(NullPointerException.class, () -> Taikai.builder().java(null));
    assertThrows(NullPointerException.class, () -> Taikai.builder().spring(null));
    assertThrows(NullPointerException.class, () -> Taikai.builder().test(null));
  }

  @Test
  void shouldCheckRules() {
    TaikaiRule mockRule = mock(TaikaiRule.class);

    Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .addRule(mockRule)
        .build()
        .check();

    verify(mockRule, times(1)).check(VALID_NAMESPACE, null, emptyList());
  }

  @Test
  void shouldRebuildTaikaiWithNewValues() {
    Taikai taikai = Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .excludeClasses("com.enofex.taikai.ClassToExclude")
        .failOnEmpty(true)
        .java(java -> java
            .fieldsShouldNotBePublic())
        .build();

    Taikai modifiedTaikai = taikai.toBuilder()
        .namespace("com.enofex.newnamespace")
        .excludeClasses("com.enofex.taikai.AnotherClassToExclude")
        .failOnEmpty(false)
        .java(java -> java
            .classesShouldImplementHashCodeAndEquals()
            .finalClassesShouldNotHaveProtectedMembers())
        .build();

    assertFalse(modifiedTaikai.failOnEmpty());
    assertEquals("com.enofex.newnamespace", modifiedTaikai.namespace());
    assertEquals(2, modifiedTaikai.excludedClasses().size());
    assertEquals(3, modifiedTaikai.rules().size());
    assertTrue(modifiedTaikai.excludedClasses().contains("com.enofex.taikai.ClassToExclude"));
    assertTrue(
        modifiedTaikai.excludedClasses().contains("com.enofex.taikai.AnotherClassToExclude"));
  }

  @Test
  void shouldThrowExceptionIfNamespaceAndClasses() {
    assertThrows(IllegalArgumentException.class, () -> Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .classes(TaikaiTest.class)
        .build());
  }
}
