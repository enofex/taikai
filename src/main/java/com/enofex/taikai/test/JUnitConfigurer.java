package com.enofex.taikai.test;

import static com.enofex.taikai.internal.ArchConditions.notDeclareThrownExceptions;
import static com.enofex.taikai.test.ContainAssertionsOrVerifications.containAssertionsOrVerifications;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_DISABLED;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_DISPLAY_NAME;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_PARAMETRIZED_TEST;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_TEST;
import static com.enofex.taikai.test.JUnitDescribedPredicates.annotatedWithTestOrParameterizedTest;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.enofex.taikai.Namespace.IMPORT;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;

/**
 * Configures and enforces best practices for JUnit tests using {@link com.tngtech.archunit ArchUnit}
 * through the Taikai framework.
 *
 * <p>This configurer helps ensure that test classes and methods follow consistent standards,
 * including naming, visibility, annotation usage, and content (assertions or verifications).
 * It applies to both {@code @Test} and {@code @ParameterizedTest} methods.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .test(test -> test
 *         .junit(junit -> junit
 *             .methodsShouldMatch("should[A-Z].*")
 *             .methodsShouldNotDeclareExceptions()
 *             .methodsShouldBeAnnotatedWithDisplayName()
 *             .methodsShouldBePackagePrivate()
 *             .methodsShouldContainAssertionsOrVerifications()
 *             .methodsShouldNotBeAnnotatedWithDisabled()
 *             .classesShouldBePackagePrivate(".*Test")
 *             .classesShouldNotBeAnnotatedWithDisabled()
 *         )
 *     );
 * }</pre>
 *
 * <p>Each rule is internally wrapped in a {@link TaikaiRule}, allowing you to customize
 * configurations such as namespaces or import scopes.</p>
 */
public final class JUnitConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  private static final Configuration CONFIGURATION = Configuration.of(IMPORT.ONLY_TESTS);

  JUnitConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule that methods annotated with {@code @Test} or {@code @ParameterizedTest}
   * should have names matching the given regex.
   *
   * @param regex the regex pattern for valid test method names
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer methodsShouldMatch(String regex) {
    return methodsShouldMatch(regex, CONFIGURATION);
  }

  /**
   * See {@link #methodsShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex pattern for valid test method names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer methodsShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should().haveNameMatching(regex)
            .as("Methods annotated with %s or %s should have names matching %s".formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETRIZED_TEST, regex)),
        configuration));
  }

  /**
   * Adds a rule that methods annotated with {@code @Test} or {@code @ParameterizedTest}
   * should not declare any thrown exceptions.
   *
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer methodsShouldNotDeclareExceptions() {
    return methodsShouldNotDeclareExceptions(CONFIGURATION);
  }

  /**
   * See {@link #methodsShouldNotDeclareExceptions()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer methodsShouldNotDeclareExceptions(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should(notDeclareThrownExceptions())
            .as("Methods annotated with %s or %s should not declare thrown Exceptions".formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETRIZED_TEST)),
        configuration));
  }

  /**
   * Adds a rule that methods annotated with {@code @Test} or {@code @ParameterizedTest}
   * should also be annotated with {@code @DisplayName}.
   *
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer methodsShouldBeAnnotatedWithDisplayName() {
    return methodsShouldBeAnnotatedWithDisplayName(CONFIGURATION);
  }

  /**
   * See {@link #methodsShouldBeAnnotatedWithDisplayName()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer methodsShouldBeAnnotatedWithDisplayName(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should().beMetaAnnotatedWith(ANNOTATION_DISPLAY_NAME)
            .as("Methods annotated with %s or %s should be annotated with %s".formatted(ANNOTATION_TEST,
                ANNOTATION_PARAMETRIZED_TEST, ANNOTATION_DISPLAY_NAME)),
        configuration));
  }

  /**
   * Adds a rule that methods annotated with {@code @Test} or {@code @ParameterizedTest}
   * should have package-private visibility.
   *
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer methodsShouldBePackagePrivate() {
    return methodsShouldBePackagePrivate(CONFIGURATION);
  }

  /**
   * See {@link #methodsShouldBePackagePrivate()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer methodsShouldBePackagePrivate(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should().bePackagePrivate()
            .as("Methods annotated with %s or %s should be package-private".formatted(ANNOTATION_TEST,
                ANNOTATION_PARAMETRIZED_TEST)),
        configuration));
  }

  /**
   * Adds a rule that no test methods should be annotated with {@code @Disabled}.
   *
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer methodsShouldNotBeAnnotatedWithDisabled() {
    return methodsShouldNotBeAnnotatedWithDisabled(CONFIGURATION);
  }

  /**
   * See {@link #methodsShouldNotBeAnnotatedWithDisabled()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer methodsShouldNotBeAnnotatedWithDisabled(Configuration configuration) {
    return addRule(TaikaiRule.of(noMethods()
            .should().beMetaAnnotatedWith(ANNOTATION_DISABLED)
            .as("Methods should not be annotated with %s".formatted(ANNOTATION_DISABLED)),
        configuration));
  }

  /**
   * Adds a rule that methods annotated with {@code @Test} or {@code @ParameterizedTest}
   * should contain at least one assertion or verification.
   *
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer methodsShouldContainAssertionsOrVerifications() {
    return methodsShouldContainAssertionsOrVerifications(CONFIGURATION);
  }

  /**
   * See {@link #methodsShouldContainAssertionsOrVerifications()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer methodsShouldContainAssertionsOrVerifications(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should(containAssertionsOrVerifications())
            .as("Methods annotated with %s or %s should contain assertions or verifications".formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETRIZED_TEST)),
        configuration));
  }

  /**
   * Adds a rule that no test classes should be annotated with {@code @Disabled}.
   *
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer classesShouldNotBeAnnotatedWithDisabled() {
    return classesShouldNotBeAnnotatedWithDisabled(CONFIGURATION);
  }

  /**
   * Adds a rule that classes with names matching the given regex should be package-private.
   *
   * @param regex the regex pattern for class names
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer classesShouldBePackagePrivate(String regex) {
    return classesShouldBePackagePrivate(regex, CONFIGURATION);
  }

  /**
   * See {@link #classesShouldBePackagePrivate(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex pattern for class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer classesShouldBePackagePrivate(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areNotInterfaces().and().haveNameMatching(regex)
            .should().bePackagePrivate()
            .as("Classes with names matching %s should be package-private".formatted(regex)),
        configuration));
  }

  /**
   * See {@link #classesShouldNotBeAnnotatedWithDisabled()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public JUnitConfigurer classesShouldNotBeAnnotatedWithDisabled(Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
            .should().beMetaAnnotatedWith(ANNOTATION_DISABLED)
            .as("Classes should not be annotated with %s".formatted(ANNOTATION_DISABLED)),
        configuration));
  }


  @Override
  public JUnitConfigurer disable() {
    disable(JUnitConfigurer.class);

    return this;
  }
}
