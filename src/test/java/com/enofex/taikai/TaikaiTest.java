package com.enofex.taikai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    assertNotNull(taikai.classes());
    assertNotNull(taikai.classesWithTests());
    assertTrue(taikai.rules().isEmpty());
  }

  @Test
  void shouldBuildTaikaiWithCustomValues() {
    TaikaiRule mockRule = mock(TaikaiRule.class);
    Collection<TaikaiRule> rules = Collections.singletonList(mockRule);

    Taikai taikai = Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .failOnEmpty(true)
        .addRules(rules)
        .build();

    assertTrue(taikai.failOnEmpty());
    assertEquals(VALID_NAMESPACE, taikai.namespace());
    assertNotNull(taikai.classes());
    assertNotNull(taikai.classesWithTests());
    assertEquals(1, taikai.rules().size());
    assertTrue(taikai.rules().contains(mockRule));
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

    Taikai taikai = Taikai.builder()
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

    Taikai taikai = Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .addRule(mockRule)
        .build();

    taikai.check();

    verify(mockRule, times(1)).check(VALID_NAMESPACE);
  }
}
