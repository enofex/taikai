package com.enofex.taikai;

import static java.util.Objects.requireNonNull;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

/**
 * Class selection criteria for architectural rules. Criteria can be composed using
 * {@link DescribedPredicate#and(DescribedPredicate)} and
 * {@link DescribedPredicate#or(DescribedPredicate)}.
 */
public final class Criteria {

  private Criteria() {
  }

  /**
   * Matches classes directly or meta-annotated with the given annotation.
   *
   * @param annotationType the annotation class
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> annotatedWith(
      Class<? extends Annotation> annotationType) {
    return annotatedWith(annotationType.getName());
  }

  /**
   * Matches classes directly or meta-annotated with the given annotation.
   *
   * @param annotationType the fully qualified annotation name, without an {@code @} prefix
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> annotatedWith(String annotationType) {
    requireNonNull(annotationType);
    return new DescribedPredicate<>("annotated with " + annotationType) {
      @Override
      public boolean test(JavaClass javaClass) {
        return javaClass.isAnnotatedWith(annotationType)
            || javaClass.isMetaAnnotatedWith(annotationType);
      }
    };
  }

  /**
   * Matches the entire simple class name against a regular expression.
   *
   * @param regex the regular expression, for example {@code .*DTO}
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> namedLike(String regex) {
    Pattern pattern = Pattern.compile(requireNonNull(regex));
    return new DescribedPredicate<>("simple name matching " + regex) {
      @Override
      public boolean test(JavaClass javaClass) {
        return pattern.matcher(javaClass.getSimpleName()).matches();
      }
    };
  }

  /**
   * Matches classes in a package using ArchUnit syntax: {@code *} matches within a package
   * segment and {@code ..} matches any number of segments.
   *
   * @param packageIdentifier the package pattern, for example {@code com.company.internal..}
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> inPackage(String packageIdentifier) {
    return JavaClass.Predicates.resideInAPackage(requireNonNull(packageIdentifier));
  }
}
