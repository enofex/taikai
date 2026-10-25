package com.enofex.taikai;

import static java.util.Objects.requireNonNull;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

/**
 * Criteria selecting fields, for every rule parameter of type
 * {@code DescribedPredicate<? super JavaField>}. Criteria can be composed using
 * {@link DescribedPredicate#and(DescribedPredicate)} and
 * {@link DescribedPredicate#or(DescribedPredicate)}, also with ArchUnit predicates such as
 * {@code HasModifiers.Predicates.modifier(PRIVATE)}.
 *
 * @see ClassCriteria
 * @see MethodCriteria
 */
public final class FieldCriteria {

  private FieldCriteria() {
  }

  /**
   * Matches fields directly or meta-annotated with the given annotation. Only annotations
   * kept in the class files can match, so annotations with {@code RetentionPolicy.SOURCE} never
   * do.
   *
   * @param annotationType the annotation class
   * @return the field predicate
   */
  public static DescribedPredicate<JavaField> annotatedWith(
      Class<? extends Annotation> annotationType) {
    return annotatedWith(annotationType.getName());
  }

  /**
   * Matches fields directly or meta-annotated with the given annotation. Only annotations
   * kept in the class files can match, so annotations with {@code RetentionPolicy.SOURCE} never
   * do.
   *
   * @param annotationType the fully qualified annotation name, without an {@code @} prefix
   * @return the field predicate
   */
  public static DescribedPredicate<JavaField> annotatedWith(String annotationType) {
    requireNonNull(annotationType);
    return new DescribedPredicate<>("annotated with " + annotationType) {
      @Override
      public boolean test(JavaField javaField) {
        return javaField.isMetaAnnotatedWith(annotationType);
      }
    };
  }

  /**
   * Matches fields whose declared raw type is exactly the given type. Subtypes do not match, and
   * for generic fields only the raw type counts, so {@code List<String>} has the type
   * {@code java.util.List}.
   *
   * @param clazz the field type
   * @return the field predicate
   */
  public static DescribedPredicate<JavaField> ofType(Class<?> clazz) {
    return ofType(clazz.getName());
  }

  /**
   * Matches fields whose declared raw type is exactly the given type. Subtypes do not match, and
   * for generic fields only the raw type counts, so {@code List<String>} has the type
   * {@code java.util.List}.
   *
   * @param typeName the fully qualified name of the field type
   * @return the field predicate
   */
  public static DescribedPredicate<JavaField> ofType(String typeName) {
    requireNonNull(typeName);
    return new DescribedPredicate<>("of type " + typeName) {
      @Override
      public boolean test(JavaField javaField) {
        return javaField.getRawType().getName().equals(typeName);
      }
    };
  }

  /**
   * Matches fields whose declared raw type matches the given class criteria, for example
   * {@code FieldCriteria.ofType(ClassCriteria.simpleNameMatching(".*Logger"))}.
   *
   * @param types the field types, selected with {@link ClassCriteria} or an ArchUnit predicate
   * @return the field predicate
   */
  public static DescribedPredicate<JavaField> ofType(DescribedPredicate<? super JavaClass> types) {
    requireNonNull(types);
    return new DescribedPredicate<>("of types " + types.getDescription()) {
      @Override
      public boolean test(JavaField javaField) {
        return types.test(javaField.getRawType());
      }
    };
  }

  /**
   * Matches the entire field name, without the declaring class, against a regular expression.
   *
   * @param regex the regular expression, for example {@code logger} or {@code .*Cache}
   * @return the field predicate
   */
  public static DescribedPredicate<JavaField> nameMatching(String regex) {
    Pattern pattern = Pattern.compile(requireNonNull(regex));
    return new DescribedPredicate<>("with names matching " + regex) {
      @Override
      public boolean test(JavaField javaField) {
        return pattern.matcher(javaField.getName()).matches();
      }
    };
  }
}
