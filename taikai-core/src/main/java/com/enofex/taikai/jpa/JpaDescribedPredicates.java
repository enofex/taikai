package com.enofex.taikai.jpa;

import static com.enofex.taikai.internal.DescribedPredicates.annotatedWith;
import static com.enofex.taikai.internal.JakartaAnnotations.ANNOTATION_EMBEDDED_ID;
import static com.enofex.taikai.internal.JakartaAnnotations.ANNOTATION_ENTITY;
import static com.enofex.taikai.internal.JakartaAnnotations.ANNOTATION_ID;
import static com.enofex.taikai.internal.JakartaAnnotations.ANNOTATION_MAPPED_SUPERCLASS;
import static com.enofex.taikai.internal.JakartaAnnotations.ANNOTATION_VERSION;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;

final class JpaDescribedPredicates {

  private JpaDescribedPredicates() {
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithEntity(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_ENTITY, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithEntityOrMappedSuperclass(
      boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_ENTITY, isMetaAnnotated)
        .or(annotatedWith(ANNOTATION_MAPPED_SUPERCLASS, isMetaAnnotated))
        .as("annotated with %s or %s".formatted(ANNOTATION_ENTITY, ANNOTATION_MAPPED_SUPERCLASS));
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithIdOrEmbeddedId(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_ID, isMetaAnnotated)
        .or(annotatedWith(ANNOTATION_EMBEDDED_ID, isMetaAnnotated))
        .as("annotated with %s or %s".formatted(ANNOTATION_ID, ANNOTATION_EMBEDDED_ID));
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithVersion(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_VERSION, isMetaAnnotated);
  }
}
