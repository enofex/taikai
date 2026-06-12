package com.enofex.taikai.test;

import static com.enofex.taikai.internal.DescribedPredicates.annotatedWith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;

final class JUnitDescribedPredicates {

  static final String ANNOTATION_TEST = "org.junit.jupiter.api.Test";
  static final String ANNOTATION_PARAMETRIZED_TEST = "org.junit.jupiter.params.ParameterizedTest";
  static final String ANNOTATION_DISABLED = "org.junit.jupiter.api.Disabled";
  static final String ANNOTATION_DISPLAY_NAME = "org.junit.jupiter.api.DisplayName";

  private JUnitDescribedPredicates() {
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithTestOrParameterizedTest(
      boolean isMetaAnnotated) {

    return annotatedWith(ANNOTATION_TEST, isMetaAnnotated)
        .or(annotatedWith(ANNOTATION_PARAMETRIZED_TEST, isMetaAnnotated));
  }

  static DescribedPredicate<JavaClass> containTestOrParameterizedTestMethods() {
    return new DescribedPredicate<>("contain methods annotated with %s or %s".formatted(
        ANNOTATION_TEST, ANNOTATION_PARAMETRIZED_TEST)) {
      @Override
      public boolean test(JavaClass javaClass) {
        return javaClass.getMethods().stream()
            .anyMatch(method -> method.isMetaAnnotatedWith(ANNOTATION_TEST)
                || method.isMetaAnnotatedWith(ANNOTATION_PARAMETRIZED_TEST));
      }
    };
  }
}
