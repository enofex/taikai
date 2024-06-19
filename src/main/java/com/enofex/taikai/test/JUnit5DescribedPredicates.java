package com.enofex.taikai.test;

import static com.enofex.taikai.internal.DescribedPredicates.annotatedWith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;

final class JUnit5DescribedPredicates {

  static final String ANNOTATION_TEST = "org.junit.jupiter.api.Test";
  static final String ANNOTATION_PARAMETRIZED_TEST = "org.junit.jupiter.params.ParameterizedTest";
  static final String ANNOTATION_DISABLED = "org.junit.jupiter.api.Disabled";
  static final String ANNOTATION_DISPLAY_NAME = "org.junit.jupiter.api.DisplayName";

  private JUnit5DescribedPredicates() {
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithTestOrParameterizedTest(
      boolean isMetaAnnotated) {

    return annotatedWith(ANNOTATION_TEST, isMetaAnnotated)
        .or(annotatedWith(ANNOTATION_PARAMETRIZED_TEST, isMetaAnnotated));
  }
}
