package com.enofex.taikai.java;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.enofex.taikai.TaikaiException;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

/**
 * Configures and enforces import-related rules using {@link com.tngtech.archunit ArchUnit} through
 * the Taikai framework.
 *
 * <p>This configurer ensures that classes do not import from restricted packages,
 * only import from expected ones, and that the codebase remains free of cyclic dependencies.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .java(java -> java
 *         .imports(imports -> imports
 *             .shouldNotImport(ImportPatterns.shaded())
 *             .shouldNotImport(ClassCriteria.nameMatching(".*Service"),
 *                 ClassCriteria.nameMatching(".*Repository"))
 *             .shouldImport(ClassCriteria.nameMatching(".*Controller"),
 *                 ClassCriteria.nameMatching(".*Service"))
 *             .shouldHaveNoCycles()
 *         )
 *     );
 * }</pre>
 */
public final class ImportsConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  ImportsConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Ensures that no classes import classes matching the given criteria. Classes that match the
   * criteria themselves are not checked, so the classes of a banned package may still import
   * each other.
   *
   * @param forbiddenImports the forbidden imports, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see #shouldNotImport(DescribedPredicate, Configuration)
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ImportsConfigurer shouldNotImport(
      DescribedPredicate<? super JavaClass> forbiddenImports) {
    return shouldNotImport(forbiddenImports, defaultConfiguration());
  }

  /**
   * Ensures that no classes import classes matching the given criteria, using the specified
   * {@link Configuration}. Classes that match the criteria themselves are not checked.
   *
   * @param forbiddenImports the forbidden imports
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see #shouldNotImport(DescribedPredicate)
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ImportsConfigurer shouldNotImport(DescribedPredicate<? super JavaClass> forbiddenImports,
      Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .that(DescribedPredicate.not(forbiddenImports))
        .should().dependOnClassesThat(forbiddenImports)
        .as("No classes should import classes %s".formatted(forbiddenImports.getDescription())),
        configuration));
  }

  /**
   * Ensures that no classes matching the given criteria import classes matching other criteria.
   * Selected classes that match the disallowed criteria themselves are not checked, so the
   * classes of a banned package may still import each other.
   *
   * @param classes the classes to check, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param forbiddenImports the forbidden imports
   * @return this configurer for fluent chaining
   * @see #shouldNotImport(DescribedPredicate, DescribedPredicate, Configuration)
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ImportsConfigurer shouldNotImport(DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> forbiddenImports) {
    return shouldNotImport(classes, forbiddenImports, defaultConfiguration());
  }

  /**
   * Ensures that no classes matching the given criteria import classes matching other criteria,
   * using the specified {@link Configuration}. Selected classes that match the disallowed criteria
   * themselves are not checked.
   *
   * @param classes the classes to check
   * @param forbiddenImports the forbidden imports
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see #shouldNotImport(DescribedPredicate, DescribedPredicate)
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ImportsConfigurer shouldNotImport(DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> forbiddenImports, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .that(classes)
        .and(DescribedPredicate.not(forbiddenImports))
        .should().dependOnClassesThat(forbiddenImports)
        .as("No classes %s should import classes %s".formatted(
            classes.getDescription(), forbiddenImports.getDescription())), configuration));
  }

  /**
   * Ensures that classes matching the given criteria import classes matching other criteria.
   *
   * @param classes the classes to check, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param requiredImports the required imports
   * @return this configurer for fluent chaining
   * @see #shouldImport(DescribedPredicate, DescribedPredicate, Configuration)
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ImportsConfigurer shouldImport(DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> requiredImports) {
    return shouldImport(classes, requiredImports, defaultConfiguration());
  }

  /**
   * Ensures that classes matching the given criteria import classes matching other criteria,
   * using the specified {@link Configuration}.
   *
   * @param classes the classes to check
   * @param requiredImports the required imports
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see #shouldImport(DescribedPredicate, DescribedPredicate)
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ImportsConfigurer shouldImport(DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> requiredImports, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(classes)
        .should().dependOnClassesThat(requiredImports)
        .as("Classes %s should import classes %s".formatted(
            classes.getDescription(), requiredImports.getDescription())), configuration));
  }

  /**
   * Ensures that the project's namespace has no cyclic dependencies between its first-level
   * subpackages. Each first-level subpackage forms one slice together with all its subpackages,
   * so cycles inside a slice and cycles involving classes directly in the namespace are not
   * reported.
   *
   * <p>The namespace is read when the rule is configured, so it must be set on the builder
   * before {@code java(...)} is first configured.</p>
   *
   * @return this configurer for fluent chaining
   * @throws TaikaiException if no namespace is configured
   * @see #shouldHaveNoCycles(Configuration)
   */
  public ImportsConfigurer shouldHaveNoCycles() {
    return shouldHaveNoCycles(defaultConfiguration());
  }

  /**
   * Ensures that the project's namespace has no cyclic dependencies between its first-level
   * subpackages, using the specified {@link Configuration}. The namespace of the configuration is
   * used if it is set, otherwise the namespace of the builder.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws TaikaiException if no namespace is configured
   * @see #shouldHaveNoCycles()
   */
  public ImportsConfigurer shouldHaveNoCycles(Configuration configuration) {
    String namespace = configuration.namespace() != null
        ? configuration.namespace()
        : configurerContext().namespace();

    if (namespace == null) {
      throw new TaikaiException("Namespace is not provided");
    }

    return addRule(TaikaiRule.of(slices()
        .matching(namespace + ".(*)..")
        .should().beFreeOfCycles()
        .as("Namespace %s should be free of cycles".formatted(namespace)), configuration));
  }

  @Override
  public ImportsConfigurer disable() {
    disable(ImportsConfigurer.class);

    return this;
  }
}
