package com.enofex.taikai;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.internal.DescribedPredicates;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

/**
 * Criteria selecting classes, for every rule parameter of type
 * {@code DescribedPredicate<? super JavaClass>}. Criteria can be composed using
 * {@link DescribedPredicate#and(DescribedPredicate)} and
 * {@link DescribedPredicate#or(DescribedPredicate)}.
 *
 * @see FieldCriteria
 * @see MethodCriteria
 */
public final class ClassCriteria {

  private ClassCriteria() {
  }

  /**
   * Matches classes directly or meta-annotated with the given annotation. Only annotations kept
   * in the class files can match, so annotations with {@code RetentionPolicy.SOURCE}, such as
   * {@code jakarta.annotation.Generated}, never do.
   *
   * @param annotationType the annotation class
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> annotatedWith(
      Class<? extends Annotation> annotationType) {
    return annotatedWith(annotationType.getName());
  }

  /**
   * Matches classes directly or meta-annotated with the given annotation. Only annotations kept
   * in the class files can match, so annotations with {@code RetentionPolicy.SOURCE}, such as
   * {@code jakarta.annotation.Generated}, never do.
   *
   * @param annotationType the fully qualified annotation name, without an {@code @} prefix
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> annotatedWith(String annotationType) {
    return DescribedPredicates.annotatedWith(requireNonNull(annotationType), true).forSubtype();
  }

  /**
   * Matches exactly the given type, for example to forbid {@code java.util.Optional} itself in
   * {@code noUsageOf} or a return type rule. Subtypes do not match; use
   * {@link #assignableTo(Class)} for those.
   *
   * @param clazz the type
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> type(Class<?> clazz) {
    return type(clazz.getName());
  }

  /**
   * Matches exactly the given type, for example to forbid {@code java.util.Optional} itself in
   * {@code noUsageOf} or a return type rule. Subtypes do not match; use
   * {@link #assignableTo(String)} for those.
   *
   * @param typeName the fully qualified name of the type
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> type(String typeName) {
    requireNonNull(typeName);
    return new DescribedPredicate<>(typeName) {
      @Override
      public boolean test(JavaClass javaClass) {
        return javaClass.getName().equals(typeName);
      }
    };
  }

  /**
   * Matches classes assignable to the given type, whether by extending a class or implementing an
   * interface, directly or indirectly. The type itself does not match, so a rule about the
   * subtypes of an interface does not constrain the interface.
   *
   * @param clazz the supertype
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> assignableTo(Class<?> clazz) {
    return assignableTo(clazz.getName());
  }

  /**
   * Matches classes assignable to the given type, whether by extending a class or implementing an
   * interface, directly or indirectly. The type itself does not match, so a rule about the
   * subtypes of an interface does not constrain the interface.
   *
   * @param typeName the fully qualified name of the supertype
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> assignableTo(String typeName) {
    requireNonNull(typeName);
    return new DescribedPredicate<>("assignable to " + typeName) {
      @Override
      public boolean test(JavaClass javaClass) {
        return javaClass.isAssignableTo(typeName) && !javaClass.getName().equals(typeName);
      }
    };
  }

  /**
   * Matches classes implementing the given interface, directly or through a superclass or another
   * interface. Interfaces themselves never match, unlike {@link #assignableTo(Class)}.
   *
   * @param clazz the interface
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> implementing(Class<?> clazz) {
    return implementing(clazz.getName());
  }

  /**
   * Matches classes implementing the given interface, directly or through a superclass or another
   * interface. Interfaces themselves never match, unlike {@link #assignableTo(String)}.
   *
   * @param typeName the fully qualified name of the interface
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> implementing(String typeName) {
    return JavaClass.Predicates.implement(requireNonNull(typeName))
        .as("implementing %s", typeName);
  }

  /**
   * Matches the entire fully qualified class name against a regular expression.
   *
   * @param regex the regular expression, for example {@code .*Dto}
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> nameMatching(String regex) {
    Pattern pattern = Pattern.compile(requireNonNull(regex));
    return new DescribedPredicate<>("with names matching " + regex) {
      @Override
      public boolean test(JavaClass javaClass) {
        return pattern.matcher(javaClass.getName()).matches();
      }
    };
  }

  /**
   * Matches the entire simple class name, without the package, against a regular expression.
   *
   * @param regex the regular expression, for example {@code OrderDto} or {@code .*Dto}
   * @return the class predicate
   */
  public static DescribedPredicate<JavaClass> simpleNameMatching(String regex) {
    Pattern pattern = Pattern.compile(requireNonNull(regex));
    return new DescribedPredicate<>("with simple names matching " + regex) {
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
    return JavaClass.Predicates.resideInAPackage(requireNonNull(packageIdentifier))
        .as("in package %s", packageIdentifier);
  }
}
