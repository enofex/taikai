package com.enofex.taikai.test;

import static com.enofex.taikai.AnnotationPredicates.annotatedWith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;

final class JUnit5Predicates {

  static final String ANNOTATION_TEST = "org.junit.jupiter.api.Test";
  static final String ANNOTATION_PARAMETRIZED_TEST = "org.junit.jupiter.params.ParameterizedTest";

  private JUnit5Predicates() {
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithTestOrParameterizedTest(
      boolean isMetaAnnotated) {

    return annotatedWith(ANNOTATION_TEST, isMetaAnnotated)
        .or(annotatedWith(ANNOTATION_PARAMETRIZED_TEST, isMetaAnnotated));
  }
}
