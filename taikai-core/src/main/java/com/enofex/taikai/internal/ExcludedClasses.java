package com.enofex.taikai.internal;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Matches classes against excluded classes. An excluded class is read as
 * <ul>
 *   <li>a package with its subpackages if it ends in {@code ..}, such as
 *   {@code com.company.generated..},</li>
 *   <li>the classes directly in a package if it ends in {@code .*}, such as
 *   {@code com.company.dto.*},</li>
 *   <li>a fully qualified class name if it contains neither {@code *} nor {@code ..}, or</li>
 *   <li>else a regular expression that the fully qualified class name must match, such as
 *   {@code .*Generated}.</li>
 * </ul>
 *
 * <p>A regular expression that ends in {@code .*} or contains neither {@code *} nor {@code ..},
 * such as {@code .*Generated.*} or {@code .+Impl}, is therefore read as a package or a class name
 * and does not match as a regular expression.</p>
 */
public final class ExcludedClasses {

  private ExcludedClasses() {
  }

  /**
   * Returns a predicate that accepts the classes that none of the excluded classes match, so that
   * {@code classes.that(predicate(excludedClasses))} leaves out the excluded ones.
   *
   * @param excludedClasses the excluded classes
   * @return the predicate
   */
  public static DescribedPredicate<JavaClass> predicate(Collection<String> excludedClasses) {
    return new ExcludeJavaClassDescribedPredicate(excludedClasses);
  }

  /**
   * Returns whether the excluded class matches any of the given classes.
   *
   * @param excludedClass the excluded class
   * @param classes the classes
   * @return {@code true} if it matches at least one class
   */
  public static boolean excludesAny(String excludedClass, JavaClasses classes) {
    Pattern pattern = Pattern.compile(toRegex(excludedClass));

    return classes.stream()
        .anyMatch(javaClass -> pattern.matcher(javaClass.getFullName()).matches());
  }

  private static String toRegex(String value) {
    if (isArchUnitRecursivePackage(value)) {
      return "^" + Pattern.quote(stripArchUnitSuffix(value)) + "(\\..+)?$";
    }

    if (isArchUnitSinglePackage(value)) {
      return "^" + Pattern.quote(stripArchUnitSuffix(value)) + "\\.[^.]+$";
    }

    if (isFullyQualifiedClass(value)) {
      return "^" + Pattern.quote(value) + "$";
    }

    return value;
  }

  private static boolean isArchUnitRecursivePackage(String value) {
    return value.endsWith("..");
  }

  private static boolean isArchUnitSinglePackage(String value) {
    return value.endsWith(".*");
  }

  private static boolean isFullyQualifiedClass(String value) {
    return !value.contains("*") && !value.contains("..");
  }

  private static String stripArchUnitSuffix(String value) {
    return value.substring(0, value.length() - 2);
  }

  private static final class ExcludeJavaClassDescribedPredicate extends
      DescribedPredicate<JavaClass> {

    private final Collection<Pattern> allExcludedClassPatterns;

    ExcludeJavaClassDescribedPredicate(Collection<String> allExcludedClasses) {
      super("exclude classes");
      this.allExcludedClassPatterns = allExcludedClasses.stream()
          .map(ExcludedClasses::toRegex)
          .map(Pattern::compile)
          .toList();
    }

    @Override
    public boolean test(JavaClass javaClass) {
      return this.allExcludedClassPatterns.stream()
          .noneMatch(pattern -> pattern.matcher(javaClass.getFullName()).matches());
    }
  }
}
