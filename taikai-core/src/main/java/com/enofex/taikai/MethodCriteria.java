package com.enofex.taikai;

import static java.util.Objects.requireNonNull;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

/**
 * Criteria selecting methods, for every rule parameter of type
 * {@code DescribedPredicate<? super JavaMethod>}. Criteria can be composed using
 * {@link DescribedPredicate#and(DescribedPredicate)} and
 * {@link DescribedPredicate#or(DescribedPredicate)}, also with ArchUnit predicates such as
 * {@code HasModifiers.Predicates.modifier(PUBLIC)}.
 *
 * @see ClassCriteria
 * @see FieldCriteria
 */
public final class MethodCriteria {

  private MethodCriteria() {
  }

  /**
   * Matches methods directly or meta-annotated with the given annotation. Only annotations
   * kept in the class files can match, so annotations with {@code RetentionPolicy.SOURCE} never
   * do.
   *
   * @param annotationType the annotation class
   * @return the method predicate
   */
  public static DescribedPredicate<JavaMethod> annotatedWith(
      Class<? extends Annotation> annotationType) {
    return annotatedWith(annotationType.getName());
  }

  /**
   * Matches methods directly or meta-annotated with the given annotation. Only annotations
   * kept in the class files can match, so annotations with {@code RetentionPolicy.SOURCE} never
   * do.
   *
   * @param annotationType the fully qualified annotation name, without an {@code @} prefix
   * @return the method predicate
   */
  public static DescribedPredicate<JavaMethod> annotatedWith(String annotationType) {
    requireNonNull(annotationType);
    return new DescribedPredicate<>("annotated with " + annotationType) {
      @Override
      public boolean test(JavaMethod javaMethod) {
        return javaMethod.isMetaAnnotatedWith(annotationType);
      }
    };
  }

  /**
   * Matches the entire method name, without the declaring class, against a regular expression.
   *
   * @param regex the regular expression, for example {@code find.*}
   * @return the method predicate
   */
  public static DescribedPredicate<JavaMethod> nameMatching(String regex) {
    Pattern pattern = Pattern.compile(requireNonNull(regex));
    return new DescribedPredicate<>("with names matching " + regex) {
      @Override
      public boolean test(JavaMethod javaMethod) {
        return pattern.matcher(javaMethod.getName()).matches();
      }
    };
  }
}
