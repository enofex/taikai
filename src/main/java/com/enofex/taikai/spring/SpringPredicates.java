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

    return annotatedWith(ANNOTATION_CONTROLLER, isMetaAnnotated)
        .or(annotatedWith(ANNOTATION_REST_CONTROLLER, isMetaAnnotated));
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithConfiguration(
      boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_CONFIGURATION, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithRestController(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_REST_CONTROLLER, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithController(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_CONTROLLER, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithService(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_SERVICE, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithRepository(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_REPOSITORY, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithSpringBootApplication(
      boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_SPRING_BOOT_APPLICATION, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedAutowired(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_AUTOWIRED, isMetaAnnotated);
  }

  private static DescribedPredicate<CanBeAnnotated> annotatedWith(String annotation,
      boolean isMetaAnnotated) {
    return new DescribedPredicate<>("annotated with %s".formatted(annotation)) {
      @Override
      public boolean test(CanBeAnnotated canBeAnnotated) {
        return isMetaAnnotated ? canBeAnnotated.isMetaAnnotatedWith(annotation) :
            canBeAnnotated.isAnnotatedWith(annotation);
      }
    };
  }
}
