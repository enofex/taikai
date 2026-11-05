package com.enofex.taikai;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enofex.taikai.configures.Configurer;
import com.enofex.taikai.java.JavaConfigurer;
import com.enofex.taikai.jpa.JpaConfigurer;
import com.enofex.taikai.logging.LoggingConfigurer;
import com.enofex.taikai.metrics.MetricsConfigurer;
import com.enofex.taikai.quarkus.QuarkusConfigurer;
import com.enofex.taikai.spring.SpringConfigurer;
import com.enofex.taikai.test.TestConfigurer;
import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

  @ParameterizedTest(name = "{0}")
  @MethodSource("builtInConfigurers")
  void shouldCustomizeBuiltInConfigurerOnce(BiConsumer<Taikai.Builder, List<Configurer>> configure,
      Class<? extends Configurer> configurerType) {
    List<Configurer> customized = new ArrayList<>();
    Taikai.Builder builder = Taikai.builder().namespace(VALID_NAMESPACE);

    configure.accept(builder, customized);
    builder.build();

    assertEquals(List.of(configurerType), customized.stream().map(Object::getClass).toList());
  }

  static Stream<Arguments> builtInConfigurers() {
    return Stream.of(
        configurer("java", (builder, customized) -> builder.java(customized::add),
            JavaConfigurer.class),
        configurer("logging", (builder, customized) -> builder.logging(customized::add),
            LoggingConfigurer.class),
        configurer("test", (builder, customized) -> builder.test(customized::add),
            TestConfigurer.class),
        configurer("spring", (builder, customized) -> builder.spring(customized::add),
            SpringConfigurer.class),
        configurer("quarkus", (builder, customized) -> builder.quarkus(customized::add),
            QuarkusConfigurer.class),
        configurer("jpa", (builder, customized) -> builder.jpa(customized::add),
            JpaConfigurer.class),
        configurer("metrics", (builder, customized) -> builder.metrics(customized::add),
            MetricsConfigurer.class));
  }

  private static Arguments configurer(String method,
      BiConsumer<Taikai.Builder, List<Configurer>> configure,
      Class<? extends Configurer> configurerType) {
    return Arguments.of(Named.of(method, configure), configurerType);
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
  void shouldFailRuleWithoutSelectedClassesOnlyWithFailOnEmpty() {
    Taikai.Builder builder = Taikai.builder()
        .classes(ViolatingClass.class)
        .java(java -> java.classesShouldBeAnnotatedWith(
            ClassCriteria.simpleNameMatching("NotExisting"), Deprecated.class));

    assertDoesNotThrow(builder.build()::check);

    AssertionError error = assertThrows(AssertionError.class,
        builder.failOnEmpty(true).build()::check);
    assertTrue(error.getMessage().contains("failed to check any classes"), error.getMessage());
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
        .excludeClasses(List.of("com.enofex.taikai.Excluded1",
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
