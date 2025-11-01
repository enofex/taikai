package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_SERVICE;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithControllerOrRestController;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithService;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;

/**
 * Configures and enforces conventions for Spring {@code @Service} classes
 * using {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that service-layer classes follow consistent naming conventions,
 * are properly annotated with {@code @Service}, and maintain clean architecture boundaries
 * by not depending on controller-layer components.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .spring(spring -> spring
 *         .services(services -> services
 *             .namesShouldEndWithService()
 *             .shouldBeAnnotatedWithService()
 *             .shouldNotDependOnControllers()
 *         )
 *     );
 * }</pre>
 *
 * <p>By default, this configurer applies to classes annotated with {@code @Service}.</p>
 */
public final class ServicesConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  private static final String DEFAULT_SERVICE_NAME_MATCHING = ".+Service";

  public ServicesConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule enforcing that classes annotated with {@code @Service}
   * have names ending with {@code Service}.
   *
   * @return this configurer instance for fluent chaining
   */
  public ServicesConfigurer namesShouldEndWithService() {
    return namesShouldMatch(DEFAULT_SERVICE_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldEndWithService()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ServicesConfigurer namesShouldEndWithService(Configuration configuration) {
    return namesShouldMatch(DEFAULT_SERVICE_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that {@code @Service}-annotated classes
   * have names matching the given regex.
   *
   * @param regex the regex pattern for valid service class names
   * @return this configurer instance for fluent chaining
   */
  public ServicesConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex pattern for valid service class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ServicesConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithService(true)))
        .should().haveNameMatching(regex)
        .as("Services should have name ending %s".formatted(regex)), configuration));
  }

  /**
   * Adds a rule enforcing that classes with names ending in {@code Service}
   * should be annotated with {@code @Service}.
   *
   * @return this configurer instance for fluent chaining
   */
  public ServicesConfigurer shouldBeAnnotatedWithService() {
    return shouldBeAnnotatedWithService(DEFAULT_SERVICE_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithService()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ServicesConfigurer shouldBeAnnotatedWithService(Configuration configuration) {
    return shouldBeAnnotatedWithService(DEFAULT_SERVICE_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that classes matching the given regex
   * should be annotated with {@code @Service}.
   *
   * @param regex the regex for service class names
   * @return this configurer instance for fluent chaining
   */
  public ServicesConfigurer shouldBeAnnotatedWithService(String regex) {
    return shouldBeAnnotatedWithService(regex, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithService(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex for service class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ServicesConfigurer shouldBeAnnotatedWithService(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(be(annotatedWithService(true)))
            .as("Services should be annotated with %s".formatted(ANNOTATION_SERVICE)),
        configuration));
  }

  /**
   * Adds a rule enforcing that service-layer classes should not depend on
   * controller or REST controller components.
   *
   * @return this configurer instance for fluent chaining
   */
  public ServicesConfigurer shouldNotDependOnControllers() {
    return shouldNotDependOnControllers(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotDependOnControllers()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ServicesConfigurer shouldNotDependOnControllers(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithService(true)))
            .should(not(dependOnClassesThat(annotatedWithControllerOrRestController(true))))
            .as("Services should not depend on Controllers or RestControllers"),
        configuration));
  }

  @Override
  public ServicesConfigurer disable() {
    disable(ServicesConfigurer.class);

    return this;
  }
}
