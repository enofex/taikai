package com.enofex.taikai.spring;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;

final class SpringPredicates {

  static final String ANNOTATION_CONFIGURATION = "org.springframework.context.annotation.Configuration";
  static final String ANNOTATION_CONTROLLER = "org.springframework.web.bind.annotation.Controller";
  static final String ANNOTATION_REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";
  static final String ANNOTATION_SERVICE = "org.springframework.stereotype.Service";
  static final String ANNOTATION_REPOSITORY = "org.springframework.stereotype.Repository";
  static final String ANNOTATION_SPRING_BOOT_APPLICATION = "org.springframework.boot.autoconfigure.SpringBootApplication";
  static final String ANNOTATION_AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";

  private SpringPredicates() {
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithControllerOrRestController(
      boolean isMetaAnnotated) {

    return annotatedWith(ANNOTATION_CONTROLLER, isMetaAnnotated,
        "annotated with %s".formatted(ANNOTATION_CONTROLLER))
        .or(annotatedWith(ANNOTATION_REST_CONTROLLER, isMetaAnnotated,
            "annotated with %s".formatted(ANNOTATION_REST_CONTROLLER)));
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithConfiguration(
      boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_CONFIGURATION, isMetaAnnotated,
        "annotated with %s".formatted(ANNOTATION_CONFIGURATION));
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithRestController(
      boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_REST_CONTROLLER, isMetaAnnotated,
        "annotated with %s".formatted(ANNOTATION_REST_CONTROLLER));
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithController(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_CONTROLLER, isMetaAnnotated,
        "annotated with %s".formatted(ANNOTATION_CONTROLLER));
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithService(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_SERVICE, isMetaAnnotated,
        "annotated with %s".formatted(ANNOTATION_SERVICE));
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithRepository(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_REPOSITORY, isMetaAnnotated,
        "annotated with %s".formatted(ANNOTATION_REPOSITORY));
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithSpringBootApplication(
      boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_SPRING_BOOT_APPLICATION, isMetaAnnotated,
        "annotated with %s".formatted(ANNOTATION_SPRING_BOOT_APPLICATION));
  }

  static DescribedPredicate<CanBeAnnotated> annotatedAutowired(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_AUTOWIRED, isMetaAnnotated,
        "annotated with %s".formatted(ANNOTATION_AUTOWIRED));
  }

  private static DescribedPredicate<CanBeAnnotated> annotatedWith(String annotation,
      boolean isMetaAnnotated, String description) {
    return new DescribedPredicate<>(description) {
      @Override
      public boolean test(CanBeAnnotated canBeAnnotated) {
        return isMetaAnnotated ? canBeAnnotated.isMetaAnnotatedWith(annotation) :
            canBeAnnotated.isAnnotatedWith(annotation);
      }
    };
  }
}
