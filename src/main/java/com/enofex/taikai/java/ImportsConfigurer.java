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
import java.util.Optional;
import org.jspecify.annotations.Nullable;

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
 *     .namespace("com.example.project")
 *     .java(java -> java
 *         .imports(imports -> imports
 *             .shouldNotImport("..internal..")
 *             .shouldNotImport(".*Service", ".*Repository")
 *             .shouldImport(".*Controller", ".*Service")
 *             .shouldHaveNoCycles()
 *         )
 *     );
 * }</pre>
 */
public class ImportsConfigurer extends AbstractConfigurer {

  ImportsConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Ensures that no classes import from the specified package.
   *
   * @param packageIdentifier the package that should not be imported
   * @return the updated {@link ImportsConfigurer} instance
   * @see #shouldNotImport(String, Configuration)
   */
  public ImportsConfigurer shouldNotImport(String packageIdentifier) {
    return shouldNotImport(packageIdentifier, defaultConfiguration());
  }

  /**
   * Ensures that no classes import from the specified package with a given {@link Configuration}.
   *
   * @param packageIdentifier the package that should not be imported
   * @param configuration     the rule configuration
   * @return the updated {@link ImportsConfigurer} instance
   * @see #shouldNotImport(String)
   */
  public ImportsConfigurer shouldNotImport(String packageIdentifier, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
            .should().accessClassesThat()
            .resideInAPackage(packageIdentifier)
            .as("No classes should have imports from package %s".formatted(packageIdentifier)),
        configuration));
  }

  /**
   * Ensures that no classes with names matching the given regex import classes that match another
   * regex.
   *
   * @param regex                 the regex for class names
   * @param notImportClassesRegex the regex for disallowed imports
   * @return the updated {@link ImportsConfigurer} instance
   * @see #shouldNotImport(String, String, Configuration)
   */
  public ImportsConfigurer shouldNotImport(String regex, String notImportClassesRegex) {
    return shouldNotImport(regex, notImportClassesRegex, defaultConfiguration());
  }

  /**
   * Ensures that no classes with names matching the given regex import classes that match another
   * regex, using the specified {@link Configuration}.
   *
   * @param regex                 the regex for class names
   * @param notImportClassesRegex the regex for disallowed imports
   * @param configuration         the rule configuration
   * @return the updated {@link ImportsConfigurer} instance
   * @see #shouldNotImport(String, String)
   */
  public ImportsConfigurer shouldNotImport(String regex, String notImportClassesRegex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .that().haveNameMatching(regex)
        .should().accessClassesThat()
        .haveNameMatching(notImportClassesRegex)
        .as("No classes that have name matching %s should have imports %s".formatted(
            regex, notImportClassesRegex)), configuration));
  }

  /**
   * Ensures that classes matching the given regex import classes matching another regex.
   *
   * @param regex              the regex for class names
   * @param importClassesRegex the regex for allowed imports
   * @return the updated {@link ImportsConfigurer} instance
   * @see #shouldImport(String, String, Configuration)
   */
  public ImportsConfigurer shouldImport(String regex, String importClassesRegex) {
    return shouldImport(regex, importClassesRegex, defaultConfiguration());
  }

  /**
   * Ensures that classes matching the given regex import classes matching another regex using the
   * specified {@link Configuration}.
   *
   * @param regex              the regex for class names
   * @param importClassesRegex the regex for allowed imports
   * @param configuration      the rule configuration
   * @return the updated {@link ImportsConfigurer} instance
   * @see #shouldImport(String, String)
   */
  public ImportsConfigurer shouldImport(String regex, String importClassesRegex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveNameMatching(regex)
        .should().accessClassesThat()
        .haveNameMatching(importClassesRegex)
        .as("Classes that have name matching %s should have imports %s".formatted(
            regex, importClassesRegex)), configuration));
  }

  /**
   * Ensures that the project's namespace has no cyclic dependencies between packages.
   *
   * @return the updated {@link ImportsConfigurer} instance
   * @see #shouldHaveNoCycles(Configuration)
   */
  public ImportsConfigurer shouldHaveNoCycles() {
    return shouldHaveNoCycles(null);
  }

  /**
   * Ensures that the project's namespace has no cyclic dependencies between packages, using the
   * specified {@link Configuration}.
   *
   * @param configuration the rule configuration
   * @return the updated {@link ImportsConfigurer} instance
   * @throws TaikaiException if the namespace is not set
   * @see #shouldHaveNoCycles()
   */
  public ImportsConfigurer shouldHaveNoCycles(@Nullable Configuration configuration) {
    String namespace = configuration != null
        ? configuration.namespace()
        : Optional.ofNullable(configurerContext())
            .map(ConfigurerContext::namespace)
            .orElse(null);

    if (namespace == null) {
      throw new TaikaiException("Namespace is not set");
    }

    return addRule(TaikaiRule.of(slices()
        .matching(namespace + ".(*)..")
        .should().beFreeOfCycles()
        .as("Namespace %s should be free of cycles".formatted(namespace)), configuration));
  }


  public static final class Disableable extends ImportsConfigurer implements DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public ImportsConfigurer disable() {
      disable(ImportsConfigurer.class);

      return this;
    }
  }
}
