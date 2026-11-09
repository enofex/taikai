package com.enofex.taikai.test;

import static com.enofex.taikai.internal.ArchConditions.notDeclareThrownExceptions;
import static com.enofex.taikai.test.ContainAssertionsOrVerifications.containAssertionsOrVerifications;
import static com.enofex.taikai.test.ExecutableTests.beExecutable;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_DISABLED;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_DISPLAY_NAME;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_NESTED;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_PARAMETERIZED_TEST;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_TEST;
import static com.enofex.taikai.test.JUnitDescribedPredicates.annotatedWithTestOrParameterizedTest;
import static com.enofex.taikai.test.JUnitDescribedPredicates.containTestOrParameterizedTestMethods;
import static com.enofex.taikai.test.TestClassNaming.haveTopLevelClassNameMatching;
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
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;

/**
 * Configures and enforces best practices for JUnit tests using
 * {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer helps ensure that test classes and methods follow consistent standards,
 * including naming, visibility, annotation usage and content (assertions or verifications).
 * It applies to both {@code @Test} and {@code @ParameterizedTest} methods.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .test(test -> test
 *         .junit(junit -> junit
 *             .methodsShouldMatch("should[A-Z].*")
 *             .methodsShouldNotDeclareExceptions()
 *             .methodsShouldBeAnnotatedWithDisplayName()
 *             .methodsShouldBePackagePrivate()
 *             .methodsShouldContainAssertionsOrVerifications()
 *             .methodsShouldNotBeAnnotatedWithDisabled()
 *             .classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Test"))
 *             .classesShouldNotBeAnnotatedWithDisabled()
 *             .classesShouldEndWithTest()
 *             .nestedClassesShouldBeAnnotatedWithNested()
 *             .testsShouldBeExecutable()
 *         )
 *     );
 * }</pre>
 *
 * <p>Each rule is internally wrapped in a {@link TaikaiRule}, allowing you to customize
 * configurations such as namespaces or import scopes.</p>
 */
public final class JUnitConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  private static final Configuration CONFIGURATION = Configuration.of(IMPORT.ONLY_TESTS);

  private static final String DEFAULT_TEST_CLASS_NAME_MATCHING = ".+Test";

  JUnitConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule that methods annotated with {@code @Test} or {@code @ParameterizedTest} must have
   * names matching the given regex.
   *
   * @param regex the regex that test method names must match
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer methodsShouldMatch(String regex) {
    return methodsShouldMatch(regex, CONFIGURATION);
  }

  /**
   * See {@link #methodsShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex that test method names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer methodsShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().haveNameMatching(regex)
            .as("Methods annotated with %s or %s should have names matching %s".formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETERIZED_TEST, regex)),
        configuration));
  }

  /**
   * Adds a rule that methods annotated with {@code @Test} or {@code @ParameterizedTest} must not
   * declare any thrown exceptions.
   *
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer methodsShouldNotDeclareExceptions() {
    return methodsShouldNotDeclareExceptions(CONFIGURATION);
  }

  /**
   * See {@link #methodsShouldNotDeclareExceptions()}, but with {@link Configuration} for
   * customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer methodsShouldNotDeclareExceptions(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(notDeclareThrownExceptions())
            .as("Methods annotated with %s or %s should not declare thrown exceptions".formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETERIZED_TEST)),
        configuration));
  }

  /**
   * Adds a rule that methods annotated with {@code @Test} or {@code @ParameterizedTest} must also
   * be annotated with {@code @DisplayName}.
   *
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer methodsShouldBeAnnotatedWithDisplayName() {
    return methodsShouldBeAnnotatedWithDisplayName(CONFIGURATION);
  }

  /**
   * See {@link #methodsShouldBeAnnotatedWithDisplayName()}, but with {@link Configuration} for
   * customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer methodsShouldBeAnnotatedWithDisplayName(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().beMetaAnnotatedWith(ANNOTATION_DISPLAY_NAME)
            .as("Methods annotated with %s or %s should be annotated with %s".formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETERIZED_TEST, ANNOTATION_DISPLAY_NAME)),
        configuration));
  }

  /**
   * Adds a rule that methods annotated with {@code @Test} or {@code @ParameterizedTest} must have
   * package-private visibility. Test methods declared in interfaces are not checked, as interface
   * methods are always public.
   *
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer methodsShouldBePackagePrivate() {
    return methodsShouldBePackagePrivate(CONFIGURATION);
  }

  /**
   * See {@link #methodsShouldBePackagePrivate()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer methodsShouldBePackagePrivate(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .and().areDeclaredInClassesThat().areNotInterfaces()
            .should().bePackagePrivate()
            .as("Methods annotated with %s or %s should be package-private".formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETERIZED_TEST)),
        configuration));
  }

  /**
   * Adds a rule that test methods must not be annotated with {@code @Disabled}.
   *
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer methodsShouldNotBeAnnotatedWithDisabled() {
    return methodsShouldNotBeAnnotatedWithDisabled(CONFIGURATION);
  }

  /**
   * See {@link #methodsShouldNotBeAnnotatedWithDisabled()}, but with {@link Configuration} for
   * customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer methodsShouldNotBeAnnotatedWithDisabled(Configuration configuration) {
    return addRule(TaikaiRule.of(noMethods()
            .that().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().beMetaAnnotatedWith(ANNOTATION_DISABLED)
            .as("Methods should not be annotated with %s".formatted(ANNOTATION_DISABLED)),
        configuration));
  }

  /**
   * Adds a rule that methods annotated with {@code @Test} or {@code @ParameterizedTest} must
   * contain at least one assertion or verification.
   *
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer methodsShouldContainAssertionsOrVerifications() {
    return methodsShouldContainAssertionsOrVerifications(CONFIGURATION);
  }

  /**
   * See {@link #methodsShouldContainAssertionsOrVerifications()}, but with {@link Configuration}
   * for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer methodsShouldContainAssertionsOrVerifications(
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(containAssertionsOrVerifications())
            .as("Methods annotated with %s or %s should contain assertions or verifications"
                .formatted(ANNOTATION_TEST, ANNOTATION_PARAMETERIZED_TEST)),
        configuration));
  }

  /**
   * Adds a rule that test classes must not be annotated with {@code @Disabled}.
   *
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer classesShouldNotBeAnnotatedWithDisabled() {
    return classesShouldNotBeAnnotatedWithDisabled(CONFIGURATION);
  }

  /**
   * Adds a rule that classes matching the given criteria must be package-private. Interfaces are
   * excluded.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JUnitConfigurer classesShouldBePackagePrivate(
      DescribedPredicate<? super JavaClass> classes) {
    return classesShouldBePackagePrivate(classes, CONFIGURATION);
  }

  /**
   * See {@link #classesShouldBePackagePrivate(DescribedPredicate)}, but with {@link Configuration}
   * for customization.
   *
   * @param classes the classes to check
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JUnitConfigurer classesShouldBePackagePrivate(
      DescribedPredicate<? super JavaClass> classes, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areNotInterfaces().and(classes)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().bePackagePrivate()
            .as("Classes %s should be package-private".formatted(classes.getDescription())),
        configuration));
  }

  /**
   * See {@link #classesShouldNotBeAnnotatedWithDisabled()}, but with {@link Configuration} for
   * customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer classesShouldNotBeAnnotatedWithDisabled(Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
            .should().beMetaAnnotatedWith(ANNOTATION_DISABLED)
            .as("Classes should not be annotated with %s".formatted(ANNOTATION_DISABLED)),
        configuration));
  }

  /**
   * Adds a rule that classes containing methods annotated with {@code @Test} or
   * {@code @ParameterizedTest} must have names ending with {@code Test}.
   *
   * <p>For nested test classes the enclosing top-level class is checked, so {@code @Nested}
   * classes do not need to match themselves.</p>
   *
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer classesShouldEndWithTest() {
    return classesShouldMatch(DEFAULT_TEST_CLASS_NAME_MATCHING, CONFIGURATION);
  }

  /**
   * See {@link #classesShouldEndWithTest()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer classesShouldEndWithTest(Configuration configuration) {
    return classesShouldMatch(DEFAULT_TEST_CLASS_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule that classes containing methods annotated with {@code @Test} or
   * {@code @ParameterizedTest} must have names matching the given regex.
   *
   * <p>For nested test classes the enclosing top-level class is checked, so {@code @Nested}
   * classes do not need to match themselves.</p>
   *
   * @param regex the regex that fully qualified test class names must match
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer classesShouldMatch(String regex) {
    return classesShouldMatch(regex, CONFIGURATION);
  }

  /**
   * See {@link #classesShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex that fully qualified test class names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer classesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(containTestOrParameterizedTestMethods())
            .should(haveTopLevelClassNameMatching(regex))
            .as("Classes containing methods annotated with %s or %s should have names matching %s"
                .formatted(ANNOTATION_TEST, ANNOTATION_PARAMETERIZED_TEST, regex)),
        configuration));
  }

  /**
   * Adds a rule that inner classes containing methods annotated with {@code @Test} or
   * {@code @ParameterizedTest} must be annotated with {@code @Nested}.
   *
   * <p>JUnit does not execute a non-static inner class without {@code @Nested} and only reports
   * a discovery warning, so its tests are silently skipped. Abstract inner classes are excluded,
   * because JUnit allows them as base classes for {@code @Nested} classes.</p>
   *
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer nestedClassesShouldBeAnnotatedWithNested() {
    return nestedClassesShouldBeAnnotatedWithNested(CONFIGURATION);
  }

  /**
   * See {@link #nestedClassesShouldBeAnnotatedWithNested()}, but with {@link Configuration} for
   * customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer nestedClassesShouldBeAnnotatedWithNested(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areInnerClasses()
            .and().doNotHaveModifier(JavaModifier.ABSTRACT)
            .and(containTestOrParameterizedTestMethods())
            .should().beMetaAnnotatedWith(ANNOTATION_NESTED)
            .as(("Inner classes containing methods annotated with %s or %s should be annotated "
                + "with %s").formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETERIZED_TEST, ANNOTATION_NESTED)),
        configuration));
  }

  /**
   * Adds a rule that test classes and test methods must be declared in a way that JUnit executes
   * them.
   *
   * <p>JUnit does not execute invalid test declarations. It only reports a discovery warning, so
   * the build stays green while the affected tests are silently ignored. The rule reports:</p>
   * <ul>
   *   <li>methods annotated with {@code @Test}, {@code @TestTemplate} or {@code @TestFactory} that
   *   are {@code private} or {@code static}, including meta-annotations such as
   *   {@code @ParameterizedTest} and {@code @RepeatedTest}</li>
   *   <li>such methods that return a value, except {@code @TestFactory} methods, which must return
   *   a {@code DynamicNode} or a {@code Stream}, {@code Collection}, {@code Iterable},
   *   {@code Iterator} or array of {@code DynamicNode}</li>
   *   <li>static classes annotated with {@code @Nested}</li>
   *   <li>private classes annotated with {@code @Nested} or containing test methods, unless they
   *   are abstract</li>
   *   <li>local and anonymous classes containing test methods</li>
   * </ul>
   *
   * <p>Inner classes containing test methods without {@code @Nested} are covered by
   * {@link #nestedClassesShouldBeAnnotatedWithNested()}.</p>
   *
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer testsShouldBeExecutable() {
    return testsShouldBeExecutable(CONFIGURATION);
  }

  /**
   * See {@link #testsShouldBeExecutable()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JUnitConfigurer testsShouldBeExecutable(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .should(beExecutable())
            .as("Test classes and methods should be executable by JUnit"),
        configuration));
  }

  @Override
  public JUnitConfigurer disable() {
    disable(JUnitConfigurer.class);

    return this;
  }
}
