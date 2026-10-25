package com.enofex.taikai.jpa;

import static com.enofex.taikai.internal.DescribedPredicates.annotatedWith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;

final class JpaDescribedPredicates {

  static final String ANNOTATION_ENTITY = "jakarta.persistence.Entity";
  static final String ANNOTATION_MAPPED_SUPERCLASS = "jakarta.persistence.MappedSuperclass";
  static final String ANNOTATION_ID = "jakarta.persistence.Id";
  static final String ANNOTATION_EMBEDDED_ID = "jakarta.persistence.EmbeddedId";
  static final String ANNOTATION_VERSION = "jakarta.persistence.Version";

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
