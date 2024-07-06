package com.enofex.taikai.spring;

import static com.enofex.taikai.internal.DescribedPredicates.annotatedWith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;

final class SpringDescribedPredicates {

  static final String ANNOTATION_CONFIGURATION = "org.springframework.context.annotation.Configuration";
  static final String ANNOTATION_CONFIGURATION_PROPERTIES = "org.springframework.boot.context.properties.ConfigurationProperties";
  static final String ANNOTATION_CONTROLLER = "org.springframework.web.bind.annotation.Controller";
  static final String ANNOTATION_REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";
  static final String ANNOTATION_SERVICE = "org.springframework.stereotype.Service";
  static final String ANNOTATION_REPOSITORY = "org.springframework.stereotype.Repository";
  static final String ANNOTATION_SPRING_BOOT_APPLICATION = "org.springframework.boot.autoconfigure.SpringBootApplication";
  static final String ANNOTATION_AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";
  static final String ANNOTATION_VALIDATED = "org.springframework.validation.annotation.Validated";

  private SpringDescribedPredicates() {
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

  static DescribedPredicate<CanBeAnnotated> annotatedWithConfigurationProperties(
      boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_CONFIGURATION_PROPERTIES, isMetaAnnotated);
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

  static DescribedPredicate<CanBeAnnotated> annotatedWithAutowired(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_AUTOWIRED, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithValidated(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_VALIDATED, isMetaAnnotated);
  }
}
