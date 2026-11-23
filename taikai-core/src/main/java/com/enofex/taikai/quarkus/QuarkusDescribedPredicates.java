package com.enofex.taikai.quarkus;

import static com.enofex.taikai.internal.DescribedPredicates.annotatedWith;
import static com.enofex.taikai.internal.JakartaAnnotations.ANNOTATION_APPLICATION_SCOPED;
import static com.enofex.taikai.internal.JakartaAnnotations.ANNOTATION_ENTITY;
import static com.enofex.taikai.internal.JakartaAnnotations.ANNOTATION_INJECT;
import static com.enofex.taikai.internal.JakartaAnnotations.ANNOTATION_PATH;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;

final class QuarkusDescribedPredicates {

  static final String PANACHE_REPOSITORY_INTERFACE =
      "io.quarkus.hibernate.orm.panache.PanacheRepository";
  static final String PANACHE_ENTITY = "io.quarkus.hibernate.orm.panache.PanacheEntity";
  static final String ANNOTATION_REGISTER_AI_SERVICE =
      "io.quarkiverse.langchain4j.RegisterAiService";

  private QuarkusDescribedPredicates() {
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithApplicationScoped(
      boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_APPLICATION_SCOPED, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithRegisterAiService(
      boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_REGISTER_AI_SERVICE, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithEntity(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_ENTITY, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithInject(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_INJECT, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithPath(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_PATH, isMetaAnnotated);
  }
}
