package com.enofex.taikai;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;

public final class AnnotationPredicates {

  private AnnotationPredicates() {
  }

  public static DescribedPredicate<CanBeAnnotated> annotatedWith(String annotation,
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
