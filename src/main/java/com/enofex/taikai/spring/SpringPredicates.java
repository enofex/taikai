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

  static DescribedPredicate<CanBeAnnotated> metaAnnotatedWithControllerOrRestController() {
    return new DescribedPredicate<>(
        "annotated with %s or %s".formatted(ANNOTATION_CONTROLLER, ANNOTATION_REST_CONTROLLER)) {
      @Override
      public boolean test(CanBeAnnotated canBeAnnotated) {
        return canBeAnnotated.isMetaAnnotatedWith(ANNOTATION_CONTROLLER)
            || canBeAnnotated.isMetaAnnotatedWith(ANNOTATION_REST_CONTROLLER);
      }
    };
  }

  static DescribedPredicate<CanBeAnnotated> metaAnnotatedWithConfiguration() {
    return metaAnnotatedWith(ANNOTATION_CONFIGURATION,
        "annotated with %s".formatted(ANNOTATION_CONFIGURATION));
  }

  static DescribedPredicate<CanBeAnnotated> metaAnnotatedWithRestController() {
    return metaAnnotatedWith(ANNOTATION_REST_CONTROLLER,
        "annotated with %s".formatted(ANNOTATION_REST_CONTROLLER));
  }

  static DescribedPredicate<CanBeAnnotated> metaAnnotatedWithController() {
    return metaAnnotatedWith(ANNOTATION_CONTROLLER,
        "annotated with %s".formatted(ANNOTATION_CONTROLLER));
  }

  static DescribedPredicate<CanBeAnnotated> metaAnnotatedWithService() {
    return metaAnnotatedWith(ANNOTATION_SERVICE, "annotated with %s".formatted(ANNOTATION_SERVICE));
  }

  static DescribedPredicate<CanBeAnnotated> metaAnnotatedWithRepository() {
    return metaAnnotatedWith(ANNOTATION_REPOSITORY,
        "annotated with %s".formatted(ANNOTATION_REPOSITORY));
  }

  static DescribedPredicate<CanBeAnnotated> metaAnnotatedWithSpringBootApplication() {
    return metaAnnotatedWith(ANNOTATION_SPRING_BOOT_APPLICATION,
        "annotated with %s".formatted(ANNOTATION_SPRING_BOOT_APPLICATION));
  }

  static DescribedPredicate<CanBeAnnotated> metaAnnotatedAutowired() {
    return metaAnnotatedWith(ANNOTATION_AUTOWIRED,
        "annotated with %s".formatted(ANNOTATION_AUTOWIRED));
  }

  private static DescribedPredicate<CanBeAnnotated> metaAnnotatedWith(String annotation,
      String description) {
    return new DescribedPredicate<>(description) {
      @Override
      public boolean test(CanBeAnnotated canBeAnnotated) {
        return canBeAnnotated.isMetaAnnotatedWith(annotation);
      }
    };
  }
}
