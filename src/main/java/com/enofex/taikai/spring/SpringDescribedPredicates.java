package com.enofex.taikai.spring;

import static com.enofex.taikai.internal.DescribedPredicates.annotatedWith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import java.util.Collection;
import java.util.List;

final class SpringDescribedPredicates {

  static final String ANNOTATION_CONFIGURATION = "org.springframework.context.annotation.Configuration";
  static final String ANNOTATION_CONFIGURATION_PROPERTIES = "org.springframework.boot.context.properties.ConfigurationProperties";
  static final String ANNOTATION_CONTROLLER = "org.springframework.stereotype.Controller";
  static final String ANNOTATION_REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";
  static final String ANNOTATION_SERVICE = "org.springframework.stereotype.Service";
  static final String ANNOTATION_REPOSITORY = "org.springframework.stereotype.Repository";
  static final String ANNOTATION_SPRING_BOOT_APPLICATION = "org.springframework.boot.autoconfigure.SpringBootApplication";
  static final String ANNOTATION_AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";
  static final String ANNOTATION_VALIDATED = "org.springframework.validation.annotation.Validated";
  static final String ANNOTATION_TRANSACTIONAL = "org.springframework.transaction.annotation.Transactional";
  static final String ANNOTATION_JAKARTA_TRANSACTIONAL = "jakarta.transaction.Transactional";
  static final String ANNOTATION_ASYNC = "org.springframework.scheduling.annotation.Async";
  static final String ANNOTATION_CACHEABLE = "org.springframework.cache.annotation.Cacheable";
  static final String ANNOTATION_CACHE_EVICT = "org.springframework.cache.annotation.CacheEvict";
  static final String ANNOTATION_CACHE_PUT = "org.springframework.cache.annotation.CachePut";
  static final String ANNOTATION_PRE_AUTHORIZE = "org.springframework.security.access.prepost.PreAuthorize";
  static final String ANNOTATION_POST_AUTHORIZE = "org.springframework.security.access.prepost.PostAuthorize";
  static final String ANNOTATION_RETRYABLE = "org.springframework.retry.annotation.Retryable";

  /**
   * Annotations whose behaviour is applied by a Spring proxy and is therefore lost when the
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

  static DescribedPredicate<CanBeAnnotated> annotatedWithTransactional(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_TRANSACTIONAL, isMetaAnnotated)
        .or(annotatedWith(ANNOTATION_JAKARTA_TRANSACTIONAL, isMetaAnnotated));
  }
}
