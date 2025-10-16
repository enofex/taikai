package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.ValidatedController.beAnnotatedWithValidated;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_CONTROLLER;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_REST_CONTROLLER;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_VALIDATED;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithController;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithControllerOrRestController;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithRestController;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.conditions.ArchConditions.onlyHaveDependentClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;

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
 *     .namespace("com.example.project")
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
public class ControllersConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_CONTROLLER_NAME_MATCHING = ".+Controller";

  ControllersConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule enforcing that controllers have names ending with {@code Controller}.
   *
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer namesShouldEndWithController() {
    return namesShouldMatch(DEFAULT_CONTROLLER_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldEndWithController()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer namesShouldEndWithController(Configuration configuration) {
    return namesShouldMatch(DEFAULT_CONTROLLER_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that controller class names match the given regex.
   *
   * @param regex the regex pattern for valid controller class names
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex pattern for valid controller class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithControllerOrRestController(true)))
        .should().haveNameMatching(regex)
        .as("Controllers should have name ending %s".formatted(regex)), configuration));
  }

  /**
   * Adds a rule that controller classes (matching the default name pattern)
   * should be annotated with {@code @RestController}.
   *
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithRestController() {
    return shouldBeAnnotatedWithRestController(DEFAULT_CONTROLLER_NAME_MATCHING,
        defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithRestController()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithRestController(Configuration configuration) {
    return shouldBeAnnotatedWithRestController(DEFAULT_CONTROLLER_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule that controller classes matching the given regex
   * should be annotated with {@code @RestController}.
   *
   * @param regex the regex for controller class names
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithRestController(String regex) {
    return shouldBeAnnotatedWithRestController(regex, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithRestController(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex for controller class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithRestController(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(be(annotatedWithRestController(true)))
            .as("Controllers should be annotated with %s".formatted(ANNOTATION_REST_CONTROLLER)),
        configuration));
  }

  /**
   * Adds a rule that controller classes (matching the default name pattern)
   * should be annotated with {@code @Controller}.
   *
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithController() {
    return shouldBeAnnotatedWithController(DEFAULT_CONTROLLER_NAME_MATCHING,
        defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithController()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithController(Configuration configuration) {
    return shouldBeAnnotatedWithController(DEFAULT_CONTROLLER_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule that controller classes matching the given regex
   * should be annotated with {@code @Controller}.
   *
   * @param regex the regex for controller class names
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithController(String regex) {
    return shouldBeAnnotatedWithController(regex, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithController(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex for controller class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithController(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(be(annotatedWithController(true)))
            .as("Controllers should be annotated with %s".formatted(ANNOTATION_CONTROLLER)),
        configuration));
  }

  /**
   * Adds a rule enforcing that controller classes should have package-private visibility.
   *
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBePackagePrivate() {
    return shouldBePackagePrivate(defaultConfiguration());
  }

  /**
   * See {@link #shouldBePackagePrivate()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
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
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldNotDependOnOtherControllers() {
    return shouldNotDependOnOtherControllers(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotDependOnOtherControllers()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldNotDependOnOtherControllers(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithControllerOrRestController(true)))
        .should(not(onlyHaveDependentClassesThat(are(annotatedWithControllerOrRestController(true)))))
        .as("Controllers should not be depend on other Controllers"), configuration));
  }

  /**
   * Adds a rule enforcing that controller classes matching the given regex
   * should be annotated with {@code @Validated} when they contain validation annotations
   * (e.g., {@code @RequestParam}, {@code @PathVariable} with {@code @Valid}).
   *
   * @param regex the regex for controller class names
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithValidated(String regex) {
    return shouldBeAnnotatedWithValidated(regex, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithValidated(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex for controller class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithValidated(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(beAnnotatedWithValidated())
            .as("Validation annotations on @RequestParam or @PathVariable require the controller to be annotated with %s."
                .formatted(ANNOTATION_VALIDATED)),
        configuration));
  }

  /**
   * Adds a rule enforcing that all controllers with validation annotations
   * (e.g., on {@code @RequestParam} or {@code @PathVariable}) must also be annotated with {@code @Validated}.
   *
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithValidated() {
    return shouldBeAnnotatedWithValidated(defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithValidated()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ControllersConfigurer shouldBeAnnotatedWithValidated(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithControllerOrRestController(true)))
            .should(beAnnotatedWithValidated())
            .as("Validation annotations on @RequestParam or @PathVariable require the controller to be annotated with %s."
                .formatted(ANNOTATION_VALIDATED)),
        configuration));
  }

  public static final class Disableable extends ControllersConfigurer implements
      DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public ControllersConfigurer disable() {
      disable(ControllersConfigurer.class);
      return this;
    }
  }
}
