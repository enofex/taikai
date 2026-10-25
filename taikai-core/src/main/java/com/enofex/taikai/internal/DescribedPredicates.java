package com.enofex.taikai.internal;

import static com.enofex.taikai.internal.Modifiers.isClassFinal;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import java.util.Collection;

/**
 * Internal utility class for defining general DescribedPredicate used in architectural rules.
 * <p>
 * This class is intended for internal use only and is not part of the public API. Developers should
 * not rely on this class for any public API usage.
 */
public final class DescribedPredicates {

  private DescribedPredicates() {
  }

  /**
   * Creates a predicate that checks if an element is annotated with a specific annotation.
   *
   * @param annotation      the annotation to check for
   * @param isMetaAnnotated true if the annotation should be meta-annotated, false otherwise
   * @return a described predicate for the annotation check
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
   * Creates a predicate that checks if an element is annotated with all the specified annotations.
   *
   * @param annotations     the collection of annotations to check for
   * @param isMetaAnnotated true if the annotations should be meta-annotated, false otherwise
   * @return a described predicate for the annotation check
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
   * Creates a predicate that checks if a class is final.
   *
   * @return a described predicate for the final modifier check
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
