package com.enofex.taikai.java;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.java.JavaChecks.assertReportsViolations;
import static com.enofex.taikai.java.JavaChecks.overload;
import static com.enofex.taikai.java.JavaChecks.rule;
import static com.enofex.taikai.java.JavaFixtures.MARKED;
import static com.enofex.taikai.java.JavaFixtures.THIS_PACKAGE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.java.JavaFixtures.DateUser;
import com.enofex.taikai.java.JavaFixtures.HttpStatusException;
import com.enofex.taikai.java.JavaFixtures.PlainClass;
import com.enofex.taikai.java.JavaFixtures.ThrowsHttpException;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JavaConfigurerUsagesTest {

  private static final DescribedPredicate<JavaClass> LEGACY_API_AND_SUPPORT =
      ClassCriteria.simpleNameMatching("Legacy(Api|Support)");

  @ParameterizedTest(name = "{0}")
  @MethodSource("rulesForbiddingAllLegacyTypes")
  void shouldAllowForbiddenTypesToUseEachOther(Customizer<JavaConfigurer> rule) {
    Taikai taikai = Taikai.builder()
        .classes(LegacyApi.class, LegacySupport.class)
        .java(rule)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rulesForbiddingLegacyApiAndSupport")
  void shouldOnlyReportOtherClassesUsingForbiddenTypes(Customizer<JavaConfigurer> rule) {
    Taikai taikai = Taikai.builder()
        .classes(LegacyApi.class, LegacySupport.class, LegacyClient.class)
        .java(rule)
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains(LegacyClient.class.getName() + ".api"));
    assertFalse(failure.getMessage().contains(LegacyApi.class.getName() + ".support"));
    assertFalse(failure.getMessage().contains(LegacySupport.class.getName() + ".api"));
  }

  static Stream<Named<Customizer<JavaConfigurer>>> rulesForbiddingAllLegacyTypes() {
    return Stream.of(
        rule("noUsageOf(DescribedPredicate)",
            java -> java.noUsageOf(ClassCriteria.simpleNameMatching("Legacy.*"))),
        rule("shouldNotImport(DescribedPredicate)", java -> java.imports(imports -> imports
            .shouldNotImport(ClassCriteria.simpleNameMatching("Legacy.*")))));
  }

  static Stream<Named<Customizer<JavaConfigurer>>> rulesForbiddingLegacyApiAndSupport() {
    return Stream.of(
        rule("noUsageOf(DescribedPredicate)",
            java -> java.noUsageOf(LEGACY_API_AND_SUPPORT)),
        rule("noUsageOf(DescribedPredicate, DescribedPredicate)",
            java -> java.noUsageOf(ClassCriteria.simpleNameMatching("Legacy.*"),
                LEGACY_API_AND_SUPPORT)),
        rule("shouldNotImport(DescribedPredicate)", java -> java.imports(imports -> imports
            .shouldNotImport(LEGACY_API_AND_SUPPORT))),
        rule("shouldNotImport(DescribedPredicate, DescribedPredicate)",
            java -> java.imports(imports -> imports
                .shouldNotImport(ClassCriteria.simpleNameMatching("Legacy.*"),
                    LEGACY_API_AND_SUPPORT))));
  }

  @Nested
  class NoUsageOf {

    @Test
    void shouldRejectUncheckedExceptionsConstructedWithoutThrowsClause() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowsHttpException.class)
          .java(java -> java.noUsageOf(ClassCriteria.inPackage(THIS_PACKAGE),
              HttpStatusException.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldRejectExceptionSubclassesByTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowsHttpExceptionSubclass.class)
          .java(java -> java.noUsageOf(ClassCriteria.inPackage(THIS_PACKAGE),
              HttpStatusException.class.getName()))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldAllowExceptionUsageOutsideRestrictedLayer() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowsHttpException.class)
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.company.service.."),
              HttpStatusException.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesUseClass() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.noUsageOf(ClassCriteria.simpleNameMatching("DateUser"), Date.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenUnselectedClassesUseTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.company.service.."),
              Date.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenTypesMatchingCriteriaAreUsed() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.noUsageOf(ClassCriteria.simpleNameMatching("Date")))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(Date.class.getName()));
    }

    @Test
    void shouldNotThrowWhenNoTypesMatchingCriteriaAreUsed() {
      Taikai taikai = Taikai.builder()
          .classes(PlainClass.class)
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("java.sql..")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldRestrictTypesMatchingCriteriaToSelectedClasses() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java.noUsageOf(MARKED, ClassCriteria.simpleNameMatching("Date")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesUseTypesMatchingCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(DateUser.class)
          .java(java -> java
              .noUsageOf(ClassCriteria.simpleNameMatching("DateUser"),
                  ClassCriteria.inPackage("java.util")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("noUsageOf(Class)",
              java -> java.noUsageOf(ArrayList.class),
              DoesNotUseArrayList.class, UsesArrayList.class),
          overload("noUsageOf(Class, Configuration)",
              java -> java.noUsageOf(Date.class, defaultConfiguration()),
              PlainClass.class, DateUser.class),
          overload("noUsageOf(String)",
              java -> java.noUsageOf(ArrayList.class.getName()),
              DoesNotUseArrayList.class, UsesArrayList.class),
          overload("noUsageOf(String, Configuration)",
              java -> java.noUsageOf(Date.class.getName(), defaultConfiguration()),
              PlainClass.class, DateUser.class),
          overload("noUsageOf(DescribedPredicate, Class)",
              java -> java.noUsageOf(ClassCriteria.inPackage(THIS_PACKAGE), ArrayList.class),
              DoesNotUseArrayList.class, UsesArrayList.class),
          overload("noUsageOf(DescribedPredicate, Class, Configuration)",
              java -> java.noUsageOf(ClassCriteria.inPackage(THIS_PACKAGE), Date.class,
                  defaultConfiguration()),
              PlainClass.class, DateUser.class),
          overload("noUsageOf(DescribedPredicate, String)",
              java -> java.noUsageOf(ClassCriteria.inPackage(THIS_PACKAGE),
                  ArrayList.class.getName()),
              DoesNotUseArrayList.class, UsesArrayList.class),
          overload("noUsageOf(DescribedPredicate, String, Configuration)",
              java -> java.noUsageOf(ClassCriteria.inPackage(THIS_PACKAGE),
                  Date.class.getName(), defaultConfiguration()),
              PlainClass.class, DateUser.class));
    }
  }

  static class LegacyApi {

    private LegacySupport support;
  }

  static class LegacySupport {

    private LegacyApi api;
  }

  static class LegacyClient {

    private LegacyApi api;
  }

  static class SpecificHttpStatusException extends HttpStatusException {
  }

  static class ThrowsHttpExceptionSubclass {
    void execute() {
      throw new SpecificHttpStatusException();
    }
  }

  static class UsesArrayList {
    private final List<String> list = new ArrayList<>();

    void add(String value) {
      this.list.add(value);
    }
  }

  static class DoesNotUseArrayList {
    private final String name = "safe";

    void print() {
      System.out.println(this.name);
    }
  }
}
