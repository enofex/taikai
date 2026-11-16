package com.enofex.taikai.internal;

import static com.enofex.taikai.internal.Modifiers.isClassFinal;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import java.util.Collection;

/**
 * {@link DescribedPredicate}s that several rules share.
 */
public final class DescribedPredicates {

  private DescribedPredicates() {
  }

  /**
   * Returns a predicate that an element is annotated with the given annotation.
   *
   * @param annotation the fully qualified name of the annotation
   * @param isMetaAnnotated whether an annotation that is itself annotated with the annotation
   *     counts as well, besides the annotation directly on the element
   * @return the predicate
   */
  public static DescribedPredicate<CanBeAnnotated> annotatedWith(String annotation,
      boolean isMetaAnnotated) {
    return new DescribedPredicate<>("annotated with %s".formatted(annotation)) {
      @Override
      public boolean test(CanBeAnnotated canBeAnnotated) {
        return isMetaAnnotated ? canBeAnnotated.isMetaAnnotatedWith(annotation)
            : canBeAnnotated.isAnnotatedWith(annotation);
      }
    };
  }

  /**
   * Returns a predicate that an element is annotated with all the given annotations.
   *
   * @param annotations the fully qualified names of the annotations
   * @param isMetaAnnotated whether an annotation that is itself annotated with one of the
   *     annotations counts as well, besides the annotations directly on the element
   * @return the predicate
   */
  public static DescribedPredicate<CanBeAnnotated> annotatedWithAll(Collection<String> annotations,
      boolean isMetaAnnotated) {
    return new DescribedPredicate<>("annotated with all of %s".formatted(annotations)) {
      @Override
      public boolean test(CanBeAnnotated canBeAnnotated) {
        return annotations.stream().allMatch(annotation ->
            isMetaAnnotated ? canBeAnnotated.isMetaAnnotatedWith(annotation)
                : canBeAnnotated.isAnnotatedWith(annotation));
      }
    };
  }

  /**
   * Returns a predicate that a class is final.
   *
   * @return the predicate
   */
  public static DescribedPredicate<JavaClass> areFinal() {
    return new DescribedPredicate<>("are final") {
      @Override
      public boolean test(JavaClass javaClass) {
        return isClassFinal(javaClass);
      }
    };
  }
}
