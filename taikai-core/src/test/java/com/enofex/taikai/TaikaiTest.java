
package com.enofex.taikai;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import static org.mockito.Mockito.when;

import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.java.JavaConfigurer;
import com.enofex.taikai.spring.SpringConfigurer;
import com.enofex.taikai.test.TestConfigurer;
import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    assertTrue(taikai.excludedCriteria().isEmpty());
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
    when(mockRule.configuration()).thenReturn(TaikaiRule.Configuration.defaultConfiguration());

    Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .addRule(mockRule)
        .build()
        .check();

    verify(mockRule, times(1)).check(VALID_NAMESPACE, null, emptyList(), List.of());
  }

  @Test
  void shouldRebuildTaikaiWithNewValues() {
    Taikai taikai = Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .excludeClasses("com.enofex.taikai.ClassToExclude")
        .failOnEmpty(true)
        .java(JavaConfigurer::fieldsShouldNotBePublic)
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

  @Test
  void shouldCheckAllRulesAndPassWhenNoViolations() {
    Taikai taikai = Taikai.builder()
        .classes(TaikaiTest.class)
        .java(java -> java.naming(naming -> naming.classesShouldMatch(".*")))
        .build();

    assertDoesNotThrow(taikai::checkAll);
  }

  @Test
  void shouldCheckAllRulesAndCollectAllViolations() {
    Taikai taikai = Taikai.builder()
        .classes(ViolatingClass.class)
        .java(java -> java
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .methodsShouldNotDeclareGenericExceptions())
        .build();

    assertThrows(AssertionError.class, taikai::checkAll);
  }

  @Test
  void shouldExcludeClassesUsingCollectionOverload() {
    Taikai taikai = Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .excludeClasses(java.util.List.of("com.enofex.taikai.Excluded1",
            "com.enofex.taikai.Excluded2"))
        .build();

    assertEquals(2, taikai.excludedClasses().size());
  }

  @Test
  void shouldExcludeClassesUsingClassArrayOverload() {
    Taikai taikai = Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .excludeClasses(String.class, Integer.class)
        .build();

    assertEquals(2, taikai.excludedClasses().size());
  }

  @Test
  void shouldThrowWhenMultipleIndependentRulesAreViolated() {
    Taikai taikai = Taikai.builder()
        .classes(ViolatingClass.class)
        .java(java -> java
            .methodsShouldNotDeclareGenericExceptions()
            .naming(naming -> naming.methodsShouldMatch("#[A-Z].*")))
        .build();

    assertThrows(AssertionError.class, taikai::checkAll);
  }

  @Test
  void shouldExcludeClassesMatchingCriteria() {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai.java")
        .excludeClasses(ClassCriteria.simpleNameMatching("ImportPatterns"))
        .java(java -> java.classesShouldBeInterfaces(ClassCriteria.nameMatching(".*Patterns")))
        .build();

    assertEquals(1, taikai.excludedCriteria().size());
    assertDoesNotThrow(taikai::check);
    assertDoesNotThrow(taikai::checkAll);
  }

  @Test
  void shouldFailWithoutCriteriaExclusion() {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai.java")
        .java(java -> java.classesShouldBeInterfaces(ClassCriteria.nameMatching(".*Patterns")))
        .build();

    assertThrows(AssertionError.class, taikai::check);
    assertThrows(AssertionError.class, taikai::checkAll);
  }

  @Test
  void shouldExcludeClassesMatchingAnyOfSeveralCriteria() {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai.java")
        .excludeClasses(ClassCriteria.simpleNameMatching("ImportPatterns"))
        .excludeClasses(ClassCriteria.nameMatching(".*Naming"))
        .java(java -> java.classesShouldBeInterfaces(ClassCriteria.nameMatching(".*(Patterns|Naming)")))
        .build();

    assertEquals(2, taikai.excludedCriteria().size());
    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldCombineNameAndCriteriaExclusions() {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai.java")
        .excludeClasses("com.enofex.taikai.java.ImportPatterns")
        .excludeClasses(ClassCriteria.nameMatching(".*Naming"))
        .java(java -> java.classesShouldBeInterfaces(ClassCriteria.nameMatching(".*(Patterns|Naming)")))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldNotApplyCriteriaExclusionsToDirectlySuppliedClasses() {
    Taikai taikai = Taikai.builder()
        .classes(ViolatingClass.class)
        .excludeClasses(ClassCriteria.simpleNameMatching("ViolatingClass"))
        .java(JavaConfigurer::methodsShouldNotDeclareGenericExceptions)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldCopyCriteriaExclusionsWithoutSharingThem() {
    Taikai base = Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .excludeClasses(ClassCriteria.simpleNameMatching("First"))
        .build();

    Taikai derived = base.toBuilder()
        .excludeClasses(ClassCriteria.simpleNameMatching("Second"))
        .build();

    assertEquals(1, base.excludedCriteria().size());
    assertEquals(2, derived.excludedCriteria().size());
  }

  @Test
  void shouldCopyNameExclusionsWithoutSharingThem() {
    Taikai base = Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .excludeClasses("com.enofex.taikai.First")
        .build();

    Taikai derived = base.toBuilder()
        .excludeClasses("com.enofex.taikai.Second")
        .build();

    assertEquals(List.of("com.enofex.taikai.First"), base.excludedClasses());
    assertEquals(2, derived.excludedClasses().size());
  }

  @Test
  void shouldNotShareNameExclusionsWithBuilder() {
    Taikai.Builder builder = Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .excludeClasses("com.enofex.taikai.First");

    Taikai taikai = builder.build();
    builder.excludeClasses("com.enofex.taikai.Second");

    assertEquals(List.of("com.enofex.taikai.First"), taikai.excludedClasses());
  }

  @Test
  void shouldCopyRulesWithoutSharingThem() {
    TaikaiRule firstRule = mock(TaikaiRule.class);
    TaikaiRule secondRule = mock(TaikaiRule.class);

    Taikai base = Taikai.builder()
        .namespace(VALID_NAMESPACE)
        .addRule(firstRule)
        .build();

    Taikai derived = base.toBuilder()
        .addRule(secondRule)
        .build();

    assertEquals(List.of(firstRule), base.rules());
    assertEquals(List.of(firstRule, secondRule), derived.rules());
  }

  @Test
  void shouldRejectNullCriteriaExclusion() {
    Taikai.Builder builder = Taikai.builder();
    DescribedPredicate<JavaClass> criteria = null;

    assertThrows(NullPointerException.class, () -> builder.excludeClasses(criteria));
  }

  static class ViolatingClass {

    public void method() throws Exception {
    }
  }
}
