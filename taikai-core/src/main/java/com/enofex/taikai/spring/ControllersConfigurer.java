package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.internal.ArchConditions.notReturnTypesMatching;
import static com.enofex.taikai.internal.ArchConditions.returnTypesMatching;
import static com.enofex.taikai.spring.ValidatedController.beAnnotatedWithValidated;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_CONTROLLER;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_REST_CONTROLLER;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_VALIDATED;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithController;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithControllerOrRestController;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithRepository;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithRestController;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.conditions.ArchConditions.onlyHaveDependentClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;

/**
 * Configures and enforces conventions for Spring MVC controllers
 * using {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that controller classes follow consistent naming,
 * annotations, and dependency rules, promoting clarity and maintainability.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .spring(spring -> spring
 *         .controllers(ctrl -> ctrl
 *             .namesShouldEndWithController()
 *             .shouldBeAnnotatedWithRestController()
 *             .shouldBePackagePrivate()
 *             .shouldNotDependOnOtherControllers()
 *             .shouldBeAnnotatedWithValidated()
 *         )
 *     );
 * }</pre>
 *
 * <p>By default, this configurer checks classes annotated with {@code @Controller}
 * or {@code @RestController}.</p>
 */
public final class ControllersConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  private static final String DEFAULT_CONTROLLER_NAME_MATCHING = ".+Controller";

  ControllersConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Prevents methods declared in controllers from returning matching types, including generic
   * arguments, bounds, and array components. All method visibilities are checked.
   * Controllers are selected by {@code @Controller} or {@code @RestController}, including
   * composed annotations.
   *
   * @param forbiddenTypes the forbidden return types, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ControllersConfigurer methodsShouldNotReturn(
      DescribedPredicate<? super JavaClass> forbiddenTypes) {
    return methodsShouldNotReturn(forbiddenTypes, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotReturn(DescribedPredicate)}, with a custom configuration.
   *
   * @param forbiddenTypes the forbidden return types
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ControllersConfigurer methodsShouldNotReturn(
      DescribedPredicate<? super JavaClass> forbiddenTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().areDeclaredInClassesThat(annotatedWithControllerOrRestController(true))
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(notReturnTypesMatching(forbiddenTypes))
        .as("Controller methods should not return types %s".formatted(
            forbiddenTypes.getDescription())), configuration));
  }

  /**
   * Requires each method declared in controllers to have at least one matching type in its
   * return signature, including generic arguments, bounds, and array components.
   * All method visibilities are checked. Controllers are selected by {@code @Controller} or
   * {@code @RestController}, including composed annotations.
   *
   * @param requiredTypes the required return types, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ControllersConfigurer methodsShouldReturn(
      DescribedPredicate<? super JavaClass> requiredTypes) {
    return methodsShouldReturn(requiredTypes, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldReturn(DescribedPredicate)}, with a custom configuration.
   *
   * @param requiredTypes the required return types
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ControllersConfigurer methodsShouldReturn(
      DescribedPredicate<? super JavaClass> requiredTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().areDeclaredInClassesThat(annotatedWithControllerOrRestController(true))
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(returnTypesMatching(requiredTypes))
        .as("Controller methods should return types %s".formatted(
            requiredTypes.getDescription())), configuration));
  }

  /**
   * Adds a rule enforcing that controllers have names ending with {@code Controller}.
   *
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer namesShouldEndWithController() {
    return namesShouldMatch(DEFAULT_CONTROLLER_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldEndWithController()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer namesShouldEndWithController(Configuration configuration) {
    return namesShouldMatch(DEFAULT_CONTROLLER_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that controller class names match the given regex.
   *
   * @param regex the regex that fully qualified class names must match
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex that fully qualified class names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithControllerOrRestController(true)))
        .should().haveNameMatching(regex)
        .as("Controllers should have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Adds a rule that controller classes (matching the default name pattern)
   * should be annotated with {@code @RestController}. Interfaces are excluded.
   *
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithRestController() {
    return shouldBeAnnotatedWithRestController(
        ClassCriteria.nameMatching(DEFAULT_CONTROLLER_NAME_MATCHING), defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithRestController()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithRestController(Configuration configuration) {
    return shouldBeAnnotatedWithRestController(
        ClassCriteria.nameMatching(DEFAULT_CONTROLLER_NAME_MATCHING), configuration);
  }

  /**
   * Adds a rule that controller classes matching the given criteria
   * should be annotated with {@code @RestController}. Interfaces are excluded.
   *
   * @param classes the controller classes, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ControllersConfigurer shouldBeAnnotatedWithRestController(
      DescribedPredicate<? super JavaClass> classes) {
    return shouldBeAnnotatedWithRestController(classes, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithRestController(DescribedPredicate)}, but with {@link Configuration} for customization.
   *
   * @param classes the controller classes
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ControllersConfigurer shouldBeAnnotatedWithRestController(
      DescribedPredicate<? super JavaClass> classes, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areNotInterfaces().and(classes)
            .should(be(annotatedWithRestController(true)))
            .as("Controllers %s should be annotated with %s".formatted(
                classes.getDescription(), ANNOTATION_REST_CONTROLLER)),
        configuration));
  }

  /**
   * Adds a rule that controller classes (matching the default name pattern)
   * should be annotated with {@code @Controller}. Interfaces are excluded.
   *
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithController() {
    return shouldBeAnnotatedWithController(
        ClassCriteria.nameMatching(DEFAULT_CONTROLLER_NAME_MATCHING), defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithController()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithController(Configuration configuration) {
    return shouldBeAnnotatedWithController(
        ClassCriteria.nameMatching(DEFAULT_CONTROLLER_NAME_MATCHING), configuration);
  }

  /**
   * Adds a rule that controller classes matching the given criteria
   * should be annotated with {@code @Controller}. Interfaces are excluded.
   *
   * @param classes the controller classes, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ControllersConfigurer shouldBeAnnotatedWithController(
      DescribedPredicate<? super JavaClass> classes) {
    return shouldBeAnnotatedWithController(classes, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithController(DescribedPredicate)}, but with {@link Configuration} for customization.
   *
   * @param classes the controller classes
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ControllersConfigurer shouldBeAnnotatedWithController(
      DescribedPredicate<? super JavaClass> classes, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areNotInterfaces().and(classes)
            .should(be(annotatedWithController(true)))
            .as("Controllers %s should be annotated with %s".formatted(
                classes.getDescription(), ANNOTATION_CONTROLLER)),
        configuration));
  }

  /**
   * Adds a rule enforcing that controller classes should have package-private visibility.
   *
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldBePackagePrivate() {
    return shouldBePackagePrivate(defaultConfiguration());
  }

  /**
   * See {@link #shouldBePackagePrivate()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldBePackagePrivate(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithControllerOrRestController(true)))
        .should().bePackagePrivate()
        .as("Controllers should be package-private"), configuration));
  }

  /**
   * Adds a rule enforcing that controller classes should not depend on other controllers.
   *
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldNotDependOnOtherControllers() {
    return shouldNotDependOnOtherControllers(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotDependOnOtherControllers()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldNotDependOnOtherControllers(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithControllerOrRestController(true)))
        .should(not(onlyHaveDependentClassesThat(are(annotatedWithControllerOrRestController(true)))))
        .as("Controllers should not depend on other Controllers"), configuration));
  }

  /**
   * Adds a rule enforcing that controller classes should not depend on repository classes
   * (annotated with {@code @Repository}), so that the service layer is not bypassed.
   *
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldNotDependOnRepositories() {
    return shouldNotDependOnRepositories(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotDependOnRepositories()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldNotDependOnRepositories(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithControllerOrRestController(true)))
            .should(not(dependOnClassesThat(annotatedWithRepository(true))))
            .as("Controllers should not depend on Repositories"),
        configuration));
  }

  /**
   * Adds a rule enforcing that controller classes matching the given criteria
   * should be annotated with {@code @Validated} when a {@code @RequestParam} or
   * {@code @PathVariable} parameter carries one of the constraints {@code @NotNull},
   * {@code @NotBlank}, {@code @Size}, {@code @Min}, {@code @Max} or {@code @Pattern}.
   *
   * @param classes the controller classes, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ControllersConfigurer shouldBeAnnotatedWithValidated(
      DescribedPredicate<? super JavaClass> classes) {
    return shouldBeAnnotatedWithValidated(classes, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithValidated(DescribedPredicate)}, but with {@link Configuration} for customization.
   *
   * @param classes the controller classes
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ControllersConfigurer shouldBeAnnotatedWithValidated(
      DescribedPredicate<? super JavaClass> classes, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(classes)
            .should(beAnnotatedWithValidated())
            .as("Controllers %s that have validation annotations on @RequestParam or @PathVariable parameters should be annotated with %s"
                .formatted(classes.getDescription(), ANNOTATION_VALIDATED)),
        configuration));
  }

  /**
   * Adds a rule enforcing that all controllers must be annotated with {@code @Validated} when a
   * {@code @RequestParam} or {@code @PathVariable} parameter carries one of the constraints
   * {@code @NotNull}, {@code @NotBlank}, {@code @Size}, {@code @Min}, {@code @Max} or
   * {@code @Pattern}.
   *
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithValidated() {
    return shouldBeAnnotatedWithValidated(defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithValidated()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithValidated(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithControllerOrRestController(true)))
            .should(beAnnotatedWithValidated())
            .as("Controllers with validation annotations on @RequestParam or @PathVariable parameters should be annotated with %s"
                .formatted(ANNOTATION_VALIDATED)),
        configuration));
  }

  /**
   * Adds a rule enforcing that controller classes matching the given criteria
   * should not be annotated with {@code @Validated}.
   *
   * @param classes the controller classes, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ControllersConfigurer shouldNotBeAnnotatedWithValidated(
      DescribedPredicate<? super JavaClass> classes) {
    return shouldNotBeAnnotatedWithValidated(classes, defaultConfiguration());
  }

  /**
   * See {@link #shouldNotBeAnnotatedWithValidated(DescribedPredicate)}, but with {@link Configuration} for customization.
   *
   * @param classes the controller classes
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ControllersConfigurer shouldNotBeAnnotatedWithValidated(
      DescribedPredicate<? super JavaClass> classes, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(classes)
            .should().notBeMetaAnnotatedWith(ANNOTATION_VALIDATED)
            .as("Controllers %s should not be annotated with %s".formatted(
                classes.getDescription(), ANNOTATION_VALIDATED)),
        configuration));
  }

  /**
   * Adds a rule enforcing that all controllers should not be annotated with validation.
   *
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldNotBeAnnotatedWithValidated() {
    return shouldNotBeAnnotatedWithValidated(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotBeAnnotatedWithValidated()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ControllersConfigurer shouldNotBeAnnotatedWithValidated(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithControllerOrRestController(true)))
            .should().notBeMetaAnnotatedWith(ANNOTATION_VALIDATED)
            .as("Controllers should not be annotated with %s".formatted(ANNOTATION_VALIDATED)), configuration));
  }

  @Override
  public ControllersConfigurer disable() {
    disable(ControllersConfigurer.class);

    return this;
  }
}
