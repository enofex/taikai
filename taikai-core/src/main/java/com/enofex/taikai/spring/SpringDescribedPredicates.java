package com.enofex.taikai.spring;

import static com.enofex.taikai.internal.DescribedPredicates.annotatedWith;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_ASYNC;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_AUTOWIRED;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_CACHEABLE;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_CACHE_EVICT;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_CACHE_PUT;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_CONFIGURATION;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_CONFIGURATION_PROPERTIES;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_CONTROLLER;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_DATA_REPOSITORY;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_JAKARTA_INJECT;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_JAKARTA_TRANSACTIONAL;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_JAVAX_INJECT;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_POST_AUTHORIZE;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_PRE_AUTHORIZE;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_REPOSITORY;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_REST_CONTROLLER;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_RETRYABLE;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_SERVICE;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_SPRING_BOOT_APPLICATION;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_TRANSACTIONAL;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_VALUE;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import java.util.Collection;
import java.util.List;

final class SpringDescribedPredicates {

  /**
   * Annotations whose behavior is applied by a Spring proxy and is therefore lost when the
   * annotated method is invoked from within the same class.
   */
  static final Collection<String> ANNOTATIONS_APPLIED_BY_PROXY = List.of(
      ANNOTATION_TRANSACTIONAL,
      ANNOTATION_JAKARTA_TRANSACTIONAL,
      ANNOTATION_ASYNC,
      ANNOTATION_CACHEABLE,
      ANNOTATION_CACHE_EVICT,
      ANNOTATION_CACHE_PUT,
      ANNOTATION_PRE_AUTHORIZE,
      ANNOTATION_POST_AUTHORIZE,
      ANNOTATION_RETRYABLE);

  static final Collection<String> ANNOTATIONS_TRANSACTIONAL = List.of(
      ANNOTATION_TRANSACTIONAL,
      ANNOTATION_JAKARTA_TRANSACTIONAL);

  /**
   * Annotations whose injection Spring silently skips on static fields and methods.
   */
  static final Collection<String> ANNOTATIONS_INJECTION = List.of(
      ANNOTATION_AUTOWIRED,
      ANNOTATION_VALUE,
      ANNOTATION_JAKARTA_INJECT,
      ANNOTATION_JAVAX_INJECT);

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

  static DescribedPredicate<JavaClass> springDataRepository() {
    return JavaClass.Predicates.assignableTo(ANNOTATION_DATA_REPOSITORY)
        .and(DescribedPredicate.not(HasName.Predicates.name(ANNOTATION_DATA_REPOSITORY)))
        .as("Spring Data repositories");
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithSpringBootApplication(
      boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_SPRING_BOOT_APPLICATION, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithAutowired(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_AUTOWIRED, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithTransactional(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_TRANSACTIONAL, isMetaAnnotated)
        .or(annotatedWith(ANNOTATION_JAKARTA_TRANSACTIONAL, isMetaAnnotated));
  }
}
