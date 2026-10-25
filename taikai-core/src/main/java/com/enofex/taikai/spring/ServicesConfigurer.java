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

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

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
 *     .namespace("com.company.project")
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

  ServicesConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule enforcing that classes annotated with {@code @Service}
   * have names ending with {@code Service}.
   *
   * @return this configurer for fluent chaining
   */
  public ServicesConfigurer namesShouldEndWithService() {
    return namesShouldMatch(DEFAULT_SERVICE_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldEndWithService()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ServicesConfigurer namesShouldEndWithService(Configuration configuration) {
    return namesShouldMatch(DEFAULT_SERVICE_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that {@code @Service}-annotated classes
   * have names matching the given regex.
   *
   * @param regex the regex that fully qualified class names must match
   * @return this configurer for fluent chaining
   */
  public ServicesConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex that fully qualified class names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ServicesConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithService(true)))
        .should().haveNameMatching(regex)
        .as("Services should have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Adds a rule enforcing that classes with names ending in {@code Service}
   * should be annotated with {@code @Service}. Interfaces are excluded.
   *
   * @return this configurer for fluent chaining
   */
  public ServicesConfigurer shouldBeAnnotatedWithService() {
    return shouldBeAnnotatedWithService(
        ClassCriteria.nameMatching(DEFAULT_SERVICE_NAME_MATCHING), defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithService()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ServicesConfigurer shouldBeAnnotatedWithService(Configuration configuration) {
    return shouldBeAnnotatedWithService(
        ClassCriteria.nameMatching(DEFAULT_SERVICE_NAME_MATCHING), configuration);
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * should be annotated with {@code @Service}. Interfaces are excluded.
   *
   * @param classes the service classes, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ServicesConfigurer shouldBeAnnotatedWithService(DescribedPredicate<? super JavaClass> classes) {
    return shouldBeAnnotatedWithService(classes, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithService(DescribedPredicate)}, but with {@link Configuration} for customization.
   *
   * @param classes the service classes
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ServicesConfigurer shouldBeAnnotatedWithService(DescribedPredicate<? super JavaClass> classes, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areNotInterfaces().and(classes)
            .should(be(annotatedWithService(true)))
            .as("Services %s should be annotated with %s".formatted(
                classes.getDescription(), ANNOTATION_SERVICE)),
        configuration));
  }

  /**
   * Adds a rule enforcing that service-layer classes should not depend on
   * other service-layer classes annotated with {@code @Service}.
   *
   * @return this configurer for fluent chaining
   */
  public ServicesConfigurer shouldNotDependOnOtherServices() {
    return shouldNotDependOnOtherServices(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotDependOnOtherServices()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ServicesConfigurer shouldNotDependOnOtherServices(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithService(true)))
            .should(not(dependOnClassesThat(annotatedWithService(true))))
            .as("Services should not depend on other Services"),
        configuration));
  }

  /**
   * Adds a rule enforcing that service-layer classes should not depend on
   * controller or REST controller components.
   *
   * @return this configurer for fluent chaining
   */
  public ServicesConfigurer shouldNotDependOnControllers() {
    return shouldNotDependOnControllers(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotDependOnControllers()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ServicesConfigurer shouldNotDependOnControllers(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithService(true)))
            .should(not(dependOnClassesThat(annotatedWithControllerOrRestController(true))))
            .as("Services should not depend on Controllers or RestControllers"),
        configuration));
  }

  /**
   * Adds a rule enforcing that service classes should have package-private visibility.
   *
   * @return this configurer for fluent chaining
   */
  public ServicesConfigurer shouldBePackagePrivate() {
    return shouldBePackagePrivate(defaultConfiguration());
  }

  /**
   * See {@link #shouldBePackagePrivate()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ServicesConfigurer shouldBePackagePrivate(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithService(true)))
        .should().bePackagePrivate()
        .as("Services should be package-private"), configuration));
  }

  @Override
  public ServicesConfigurer disable() {
    disable(ServicesConfigurer.class);

    return this;
  }
}
