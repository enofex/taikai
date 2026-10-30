package com.enofex.taikai.metrics;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.MethodCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MetricsConfigurerTest {

  @Test
  void shouldDescribeRules() {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai")
        .metrics(metrics -> metrics
            .classesShouldNotExceedMaxInheritanceDepth(3)
            .classesShouldNotExceedMaxSubclasses(10)
            .methodsShouldNotExceedMaxParameters(5)
            .packagesShouldNotExceedMaxClasses(40)
            .classesShouldNotExceedMaxDependencies(20)
            .packagesShouldNotExceedMaxDependencies(10)
            .packagesShouldNotExceedMaxRelativeVisibility(0.5))
        .build();

    assertEquals(List.of(
            "Classes should not have an inheritance depth of more than 3",
            "Classes should not have more than 10 subclasses",
            "Methods should not have more than 5 parameters",
            "Packages should not contain more than 40 classes",
            "Classes should not depend on more than 20 classes",
            "Packages should not depend on more than 10 packages",
            "Packages should not have a relative visibility of more than 0.5"),
        taikai.rules().stream()
            .map(rule -> rule.archRule().getDescription())
            .toList());
  }

  @Test
  void shouldRejectNegativeLimits() {
    DescribedPredicate<JavaClass> classes = ClassCriteria.nameMatching(".*");

    assertAll(
        () -> assertRejected("maxInheritanceDepth",
            metrics -> metrics.classesShouldNotExceedMaxInheritanceDepth(-1)),
        () -> assertRejected("maxInheritanceDepth",
            metrics -> metrics.classesShouldNotExceedMaxInheritanceDepth(classes, -1)),
        () -> assertRejected("maxSubclasses",
            metrics -> metrics.classesShouldNotExceedMaxSubclasses(-1)),
        () -> assertRejected("maxSubclasses",
            metrics -> metrics.classesShouldNotExceedMaxSubclasses(classes, -1)),
        () -> assertRejected("maxMethodParameters",
            metrics -> metrics.methodsShouldNotExceedMaxParameters(-1)),
        () -> assertRejected("maxMethodParameters",
            metrics -> metrics.methodsShouldNotExceedMaxParameters(
                MethodCriteria.nameMatching(".*"), -1)),
        () -> assertRejected("maxMethodParameters",
            metrics -> metrics.methodsShouldNotExceedMaxParametersForClass(classes, -1)),
        () -> assertRejected("maxClasses",
            metrics -> metrics.packagesShouldNotExceedMaxClasses(-1)),
        () -> assertRejected("maxDependencies",
            metrics -> metrics.classesShouldNotExceedMaxDependencies(-1)),
        () -> assertRejected("maxDependencies",
            metrics -> metrics.classesShouldNotExceedMaxDependencies(classes, -1)),
        () -> assertRejected("maxDependencies",
            metrics -> metrics.packagesShouldNotExceedMaxDependencies(-1)));
  }

  @ParameterizedTest
  @ValueSource(doubles = {-0.01, 1.01, 50, Double.NaN, Double.POSITIVE_INFINITY})
  void shouldRejectLimitsOutsideZeroAndOne(double max) {
    IllegalArgumentException visibility = assertThrows(IllegalArgumentException.class,
        () -> Taikai.builder().metrics(metrics -> metrics
            .packagesShouldNotExceedMaxRelativeVisibility(max)));

    assertEquals("maxRelativeVisibility must be between 0 and 1, but was " + max,
        visibility.getMessage());
  }

  @ParameterizedTest
  @ValueSource(doubles = {0, 0.5, 1})
  void shouldAcceptLimitsBetweenZeroAndOne(double max) {
    assertDoesNotThrow(() -> Taikai.builder().metrics(metrics -> metrics
        .packagesShouldNotExceedMaxRelativeVisibility(max)));
  }

  @Test
  void shouldRegisterFurtherMetricsThroughConfigure() {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai")
        .metrics(metrics -> metrics
            .configure(FurtherMetricsConfigurer::noImplSuffix, FurtherMetricsConfigurer::new)
            .configure(FurtherMetricsConfigurer::noImplSuffix, FurtherMetricsConfigurer::new))
        .build();

    assertEquals(2, taikai.rules().size());
  }

  @Test
  void shouldDisableFurtherMetricsWithTheGroup() {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai")
        .metrics(metrics -> metrics
            .configure(FurtherMetricsConfigurer::noImplSuffix, FurtherMetricsConfigurer::new)
            .methodsShouldNotExceedMaxParameters(5)
            .disable())
        .build();

    assertTrue(taikai.rules().isEmpty());
  }

  private static void assertRejected(String limit, Customizer<MetricsConfigurer> customizer) {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> Taikai.builder().metrics(customizer));

    assertEquals(limit + " must not be negative, but was -1", exception.getMessage());
  }

  static final class FurtherMetricsConfigurer extends AbstractConfigurer {

    FurtherMetricsConfigurer(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    void noImplSuffix() {
      addRule(TaikaiRule.of(classes().should().haveSimpleNameNotEndingWith("Impl")));
    }
  }
}
