package com.enofex.taikai.metrics;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.metrics.ClassMetrics.checkedClasses;
import static com.enofex.taikai.metrics.ClassMetrics.checkedTopLevelClasses;
import static com.enofex.taikai.metrics.ClassMetrics.notExceedMaxDependencies;
import static com.enofex.taikai.metrics.ClassMetrics.notExceedMaxInheritanceDepth;
import static com.enofex.taikai.metrics.ClassMetrics.notExceedMaxMethodParameters;
import static com.enofex.taikai.metrics.ClassMetrics.notExceedMaxSubclasses;
import static com.enofex.taikai.metrics.PackageMetrics.notExceedMaxClasses;
import static com.enofex.taikai.metrics.PackageMetrics.notExceedMaxPackageDependencies;
import static com.enofex.taikai.metrics.PackageMetrics.notExceedMaxRelativeVisibility;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static java.util.Objects.requireNonNull;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.Configurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.configures.DisableableConfigurer;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Configures rules that limit code metrics using {@link com.tngtech.archunit ArchUnit} through
 * the Taikai framework: the depth of inheritance, the number of method parameters, the size of
 * packages, and the coupling between classes and between packages.
 *
 * <p>Only checked classes count, never those of the JDK or of frameworks. Rules that take criteria
 * check only the matching classes or methods, but still count all checked classes. Every package
 * is a component of its own, without its subpackages. A negative limit is rejected.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .metrics(metrics -> metrics
 *         .classesShouldNotExceedMaxInheritanceDepth(3)
 *         .classesShouldNotExceedMaxDependencies(20)
 *         .classesShouldNotExceedMaxDependencies(ClassCriteria.nameMatching(".*Controller"), 5));
 * }</pre>
 */
public final class MetricsConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  private final Set<Class<? extends Configurer>> extensions = new LinkedHashSet<>();

  public MetricsConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule that classes do not have more than the given number of superclasses among the
   * checked classes. Interfaces and anonymous classes are not checked.
   *
   * @param maxInheritanceDepth maximum number of superclasses
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxInheritanceDepth} is negative
   */
  public MetricsConfigurer classesShouldNotExceedMaxInheritanceDepth(int maxInheritanceDepth) {
    return classesShouldNotExceedMaxInheritanceDepth(maxInheritanceDepth, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldNotExceedMaxInheritanceDepth(int)}, but with {@link Configuration}
   * for customization.
   *
   * @param maxInheritanceDepth maximum number of superclasses
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxInheritanceDepth} is negative
   */
  public MetricsConfigurer classesShouldNotExceedMaxInheritanceDepth(int maxInheritanceDepth,
      Configuration configuration) {
    requireNotNegative(maxInheritanceDepth, "maxInheritanceDepth");

    return addRule(TaikaiRule.of(all(checkedClasses(areNotInterfacesOrAnonymousClasses()))
        .should(notExceedMaxInheritanceDepth(maxInheritanceDepth))
        .as("Classes should not have an inheritance depth of more than %d".formatted(
            maxInheritanceDepth)), configuration));
  }

  /**
   * Like {@link #classesShouldNotExceedMaxInheritanceDepth(int)}, but only for classes matching the
   * given criteria; superclasses that do not match still count.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria} or
   *     an ArchUnit predicate
   * @param maxInheritanceDepth maximum number of superclasses
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxInheritanceDepth} is negative
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public MetricsConfigurer classesShouldNotExceedMaxInheritanceDepth(
      DescribedPredicate<? super JavaClass> classes, int maxInheritanceDepth) {
    return classesShouldNotExceedMaxInheritanceDepth(classes, maxInheritanceDepth,
        defaultConfiguration());
  }

  /**
   * See {@link #classesShouldNotExceedMaxInheritanceDepth(DescribedPredicate, int)}, but with
   * {@link Configuration} for customization.
   *
   * @param classes the classes to check
   * @param maxInheritanceDepth maximum number of superclasses
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxInheritanceDepth} is negative
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public MetricsConfigurer classesShouldNotExceedMaxInheritanceDepth(
      DescribedPredicate<? super JavaClass> classes, int maxInheritanceDepth,
      Configuration configuration) {
    requireNotNegative(maxInheritanceDepth, "maxInheritanceDepth");

    return addRule(TaikaiRule.of(all(checkedClasses(
            areNotInterfacesOrAnonymousClasses().and(classes)))
        .should(notExceedMaxInheritanceDepth(maxInheritanceDepth))
        .as("Classes %s should not have an inheritance depth of more than %d".formatted(
            classes.getDescription(), maxInheritanceDepth)), configuration));
  }

  /**
   * Adds a rule that classes do not have more than the given number of direct subclasses among the
   * checked classes. Anonymous classes do not count, and interfaces are not checked.
   *
   * @param maxSubclasses maximum number of direct subclasses
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxSubclasses} is negative
   */
  public MetricsConfigurer classesShouldNotExceedMaxSubclasses(int maxSubclasses) {
    return classesShouldNotExceedMaxSubclasses(maxSubclasses, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldNotExceedMaxSubclasses(int)}, but with {@link Configuration} for
   * customization.
   *
   * @param maxSubclasses maximum number of direct subclasses
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxSubclasses} is negative
   */
  public MetricsConfigurer classesShouldNotExceedMaxSubclasses(int maxSubclasses,
      Configuration configuration) {
    requireNotNegative(maxSubclasses, "maxSubclasses");

    return addRule(TaikaiRule.of(all(checkedClasses(areNotInterfaces()))
        .should(notExceedMaxSubclasses(maxSubclasses))
        .as("Classes should not have more than %d subclasses".formatted(maxSubclasses)),
        configuration));
  }

  /**
   * Like {@link #classesShouldNotExceedMaxSubclasses(int)}, but only for classes matching the given
   * criteria; subclasses that do not match still count.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria} or
   *     an ArchUnit predicate
   * @param maxSubclasses maximum number of direct subclasses
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxSubclasses} is negative
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public MetricsConfigurer classesShouldNotExceedMaxSubclasses(
      DescribedPredicate<? super JavaClass> classes, int maxSubclasses) {
    return classesShouldNotExceedMaxSubclasses(classes, maxSubclasses, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldNotExceedMaxSubclasses(DescribedPredicate, int)}, but with
   * {@link Configuration} for customization.
   *
   * @param classes the classes to check
   * @param maxSubclasses maximum number of direct subclasses
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxSubclasses} is negative
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public MetricsConfigurer classesShouldNotExceedMaxSubclasses(
      DescribedPredicate<? super JavaClass> classes, int maxSubclasses,
      Configuration configuration) {
    requireNotNegative(maxSubclasses, "maxSubclasses");

    return addRule(TaikaiRule.of(all(checkedClasses(areNotInterfaces().and(classes)))
        .should(notExceedMaxSubclasses(maxSubclasses))
        .as("Classes %s should not have more than %d subclasses".formatted(
            classes.getDescription(), maxSubclasses)), configuration));
  }

  /**
   * Adds a rule that methods do not have more than the given number of parameters. Constructors
   * and synthetic methods generated by the compiler are not checked.
   *
   * @param maxMethodParameters maximum number of allowed parameters
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxMethodParameters} is negative
   */
  public MetricsConfigurer methodsShouldNotExceedMaxParameters(int maxMethodParameters) {
    return methodsShouldNotExceedMaxParameters(maxMethodParameters, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotExceedMaxParameters(int)}, but with {@link Configuration} for
   * customization.
   *
   * @param maxMethodParameters maximum number of allowed parameters
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxMethodParameters} is negative
   */
  public MetricsConfigurer methodsShouldNotExceedMaxParameters(int maxMethodParameters,
      Configuration configuration) {
    requireNotNegative(maxMethodParameters, "maxMethodParameters");

    return addRule(TaikaiRule.of(methods()
        .that().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(notExceedMaxMethodParameters(maxMethodParameters))
        .as("Methods should not have more than %d parameters".formatted(maxMethodParameters)),
        configuration));
  }

  /**
   * Like {@link #methodsShouldNotExceedMaxParameters(int)}, but only for methods matching the given
   * criteria.
   *
   * @param methods the methods to check, selected with
   *     {@link com.enofex.taikai.MethodCriteria} or an ArchUnit predicate
   * @param maxMethodParameters maximum number of allowed parameters
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxMethodParameters} is negative
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public MetricsConfigurer methodsShouldNotExceedMaxParameters(
      DescribedPredicate<? super JavaMethod> methods, int maxMethodParameters) {
    return methodsShouldNotExceedMaxParameters(methods, maxMethodParameters,
        defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotExceedMaxParameters(DescribedPredicate, int)}, but with
   * {@link Configuration} for customization.
   *
   * @param methods the methods to check
   * @param maxMethodParameters maximum number of allowed parameters
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxMethodParameters} is negative
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public MetricsConfigurer methodsShouldNotExceedMaxParameters(
      DescribedPredicate<? super JavaMethod> methods, int maxMethodParameters,
      Configuration configuration) {
    requireNotNegative(maxMethodParameters, "maxMethodParameters");

    return addRule(TaikaiRule.of(methods()
        .that(methods)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(notExceedMaxMethodParameters(maxMethodParameters))
        .as("Methods %s should not have more than %d parameters".formatted(
            methods.getDescription(), maxMethodParameters)), configuration));
  }

  /**
   * Like {@link #methodsShouldNotExceedMaxParameters(int)}, but only for the methods of classes
   * matching the given criteria.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param maxMethodParameters maximum number of allowed parameters
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxMethodParameters} is negative
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public MetricsConfigurer methodsShouldNotExceedMaxParametersForClass(
      DescribedPredicate<? super JavaClass> classes, int maxMethodParameters) {
    return methodsShouldNotExceedMaxParametersForClass(classes, maxMethodParameters,
        defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotExceedMaxParametersForClass(DescribedPredicate, int)}, but with
   * {@link Configuration} for customization.
   *
   * @param classes the classes whose declared methods are checked
   * @param maxMethodParameters maximum number of allowed parameters
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxMethodParameters} is negative
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public MetricsConfigurer methodsShouldNotExceedMaxParametersForClass(
      DescribedPredicate<? super JavaClass> classes, int maxMethodParameters,
      Configuration configuration) {
    requireNotNegative(maxMethodParameters, "maxMethodParameters");

    return addRule(TaikaiRule.of(methods()
        .that().areDeclaredInClassesThat(classes)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(notExceedMaxMethodParameters(maxMethodParameters))
        .as("Methods declared in classes %s should not have more than %d parameters".formatted(
            classes.getDescription(), maxMethodParameters)), configuration));
  }

  /**
   * Adds a rule that packages do not contain more than the given number of top-level classes.
   *
   * @param maxClasses maximum number of top-level classes in a package
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxClasses} is negative
   */
  public MetricsConfigurer packagesShouldNotExceedMaxClasses(int maxClasses) {
    return packagesShouldNotExceedMaxClasses(maxClasses, defaultConfiguration());
  }

  /**
   * See {@link #packagesShouldNotExceedMaxClasses(int)}, but with {@link Configuration} for
   * customization.
   *
   * @param maxClasses maximum number of top-level classes in a package
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxClasses} is negative
   */
  public MetricsConfigurer packagesShouldNotExceedMaxClasses(int maxClasses,
      Configuration configuration) {
    requireNotNegative(maxClasses, "maxClasses");

    return addRule(TaikaiRule.of(classes()
        .should(notExceedMaxClasses(maxClasses))
        .as("Packages should not contain more than %d classes".formatted(maxClasses)),
        configuration));
  }

  /**
   * Adds a rule that classes do not depend on more than the given number of checked classes,
   * counted per top-level class: the dependencies of nested, local and anonymous classes count for
   * their top-level class, and a nested class counts as its top-level class. Only top-level classes
   * are checked.
   *
   * @param maxDependencies maximum number of classes a class depends on
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxDependencies} is negative
   */
  public MetricsConfigurer classesShouldNotExceedMaxDependencies(int maxDependencies) {
    return classesShouldNotExceedMaxDependencies(maxDependencies, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldNotExceedMaxDependencies(int)}, but with {@link Configuration} for
   * customization.
   *
   * @param maxDependencies maximum number of classes a class depends on
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxDependencies} is negative
   */
  public MetricsConfigurer classesShouldNotExceedMaxDependencies(int maxDependencies,
      Configuration configuration) {
    requireNotNegative(maxDependencies, "maxDependencies");

    return addRule(TaikaiRule.of(all(checkedTopLevelClasses(DescribedPredicate.alwaysTrue()))
        .should(notExceedMaxDependencies(maxDependencies))
        .as("Classes should not depend on more than %d classes".formatted(maxDependencies)),
        configuration));
  }

  /**
   * Like {@link #classesShouldNotExceedMaxDependencies(int)}, but only for top-level classes
   * matching the given criteria; dependencies on classes that do not match still count.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria} or
   *     an ArchUnit predicate
   * @param maxDependencies maximum number of classes a class depends on
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxDependencies} is negative
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public MetricsConfigurer classesShouldNotExceedMaxDependencies(
      DescribedPredicate<? super JavaClass> classes, int maxDependencies) {
    return classesShouldNotExceedMaxDependencies(classes, maxDependencies,
        defaultConfiguration());
  }

  /**
   * See {@link #classesShouldNotExceedMaxDependencies(DescribedPredicate, int)}, but with
   * {@link Configuration} for customization.
   *
   * @param classes the classes to check
   * @param maxDependencies maximum number of classes a class depends on
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxDependencies} is negative
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public MetricsConfigurer classesShouldNotExceedMaxDependencies(
      DescribedPredicate<? super JavaClass> classes, int maxDependencies,
      Configuration configuration) {
    requireNotNegative(maxDependencies, "maxDependencies");

    return addRule(TaikaiRule.of(all(checkedTopLevelClasses(classes))
        .should(notExceedMaxDependencies(maxDependencies))
        .as("Classes %s should not depend on more than %d classes".formatted(
            classes.getDescription(), maxDependencies)), configuration));
  }

  /**
   * Adds a rule that packages do not depend on more than the given number of other packages of
   * checked classes.
   *
   * @param maxDependencies maximum number of packages a package depends on
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxDependencies} is negative
   */
  public MetricsConfigurer packagesShouldNotExceedMaxDependencies(int maxDependencies) {
    return packagesShouldNotExceedMaxDependencies(maxDependencies, defaultConfiguration());
  }

  /**
   * See {@link #packagesShouldNotExceedMaxDependencies(int)}, but with {@link Configuration} for
   * customization.
   *
   * @param maxDependencies maximum number of packages a package depends on
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxDependencies} is negative
   */
  public MetricsConfigurer packagesShouldNotExceedMaxDependencies(int maxDependencies,
      Configuration configuration) {
    requireNotNegative(maxDependencies, "maxDependencies");

    return addRule(TaikaiRule.of(classes()
        .should(notExceedMaxPackageDependencies(maxDependencies))
        .as("Packages should not depend on more than %d packages".formatted(maxDependencies)),
        configuration));
  }

  /**
   * Adds a rule that packages do not have a relative visibility larger than the given maximum.
   * The relative visibility is the share of public top-level classes among all top-level classes
   * of a package, as defined by Herbert Dowalil. The value is rounded to two decimal places.
   *
   * @param maxRelativeVisibility maximum share of public classes, between 0 and 1
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxRelativeVisibility} is not between 0 and 1
   */
  public MetricsConfigurer packagesShouldNotExceedMaxRelativeVisibility(
      double maxRelativeVisibility) {
    return packagesShouldNotExceedMaxRelativeVisibility(maxRelativeVisibility,
        defaultConfiguration());
  }

  /**
   * See {@link #packagesShouldNotExceedMaxRelativeVisibility(double)}, but with
   * {@link Configuration} for customization.
   *
   * @param maxRelativeVisibility maximum share of public classes, between 0 and 1
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalArgumentException if {@code maxRelativeVisibility} is not between 0 and 1
   */
  public MetricsConfigurer packagesShouldNotExceedMaxRelativeVisibility(
      double maxRelativeVisibility, Configuration configuration) {
    requireBetweenZeroAndOne(maxRelativeVisibility, "maxRelativeVisibility");

    return addRule(TaikaiRule.of(classes()
        .should(notExceedMaxRelativeVisibility(maxRelativeVisibility))
        .as("Packages should not have a relative visibility of more than %s".formatted(
            maxRelativeVisibility)), configuration));
  }

  /**
   * Registers or customizes a configurer that adds further metrics, for example the Kotlin
   * metrics of {@code taikai-kotlin}. Its rules belong to this group, so {@link #disable()} removes
   * them as well. Repeated registrations of the same configurer type accumulate rules.
   *
   * @param customizer the configuration callback
   * @param supplier factory receiving this builder's configuration context
   * @param <T> the configurer type
   * @return this configurer for fluent chaining
   */
  public <T extends Configurer> MetricsConfigurer configure(Customizer<T> customizer,
      Function<ConfigurerContext, T> supplier) {
    requireNonNull(customizer);
    requireNonNull(supplier);

    T configurer = configurerContext().configurers()
        .getOrApply(supplier.apply(configurerContext()));
    this.extensions.add(configurer.getClass());
    customizer.customize(configurer);

    return this;
  }

  @Override
  public MetricsConfigurer disable() {
    disable(MetricsConfigurer.class);
    this.extensions.forEach(extension -> disable(extension));

    return this;
  }

  /**
   * Rejects a negative limit, which every class or package would exceed.
   */
  private static void requireNotNegative(int value, String name) {
    if (value < 0) {
      throw new IllegalArgumentException(
          "%s must not be negative, but was %d".formatted(name, value));
    }
  }

  /**
   * Rejects a value outside 0 to 1, including {@code NaN}, which would turn the rule off.
   */
  private static void requireBetweenZeroAndOne(double value, String name) {
    if (!(value >= 0 && value <= 1)) {
      throw new IllegalArgumentException(
          "%s must be between 0 and 1, but was %s".formatted(name, value));
    }
  }

  private static DescribedPredicate<JavaClass> areNotInterfaces() {
    return DescribedPredicate.not(JavaClass.Predicates.INTERFACES);
  }

  private static DescribedPredicate<JavaClass> areNotInterfacesOrAnonymousClasses() {
    return areNotInterfaces().and(DescribedPredicate.not(JavaClass.Predicates.ANONYMOUS_CLASSES));
  }
}
