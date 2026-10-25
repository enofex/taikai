package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_REPOSITORY;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithControllerOrRestController;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithRepository;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithService;
import static com.enofex.taikai.spring.SpringDescribedPredicates.springDataRepository;
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
 * Configures and enforces conventions for Spring {@code @Repository} classes
 * using {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that repository classes follow consistent naming conventions,
 * are properly annotated with {@code @Repository}, and maintain clean architectural boundaries
 * by not depending on service-layer components.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .spring(spring -> spring
 *         .repositories(repo -> repo
 *             .namesShouldEndWithRepository()
 *             .shouldBeAnnotatedWithRepository()
 *             .shouldNotDependOnServices()
 *             .shouldBePackagePrivate()
 *         )
 *     );
 * }</pre>
 *
 * <p>By default, this configurer applies to classes annotated with {@code @Repository}.</p>
 */
public final class RepositoriesConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  private static final String DEFAULT_REPOSITORY_NAME_MATCHING = ".+Repository";

  RepositoriesConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule enforcing that classes annotated with {@code @Repository}
   * have names ending with {@code Repository}.
   *
   * @return this configurer for fluent chaining
   */
  public RepositoriesConfigurer namesShouldEndWithRepository() {
    return namesShouldMatch(DEFAULT_REPOSITORY_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldEndWithRepository()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public RepositoriesConfigurer namesShouldEndWithRepository(Configuration configuration) {
    return namesShouldMatch(DEFAULT_REPOSITORY_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that {@code @Repository}-annotated classes
   * have names matching the given regex.
   *
   * @param regex the regex that fully qualified class names must match
   * @return this configurer for fluent chaining
   */
  public RepositoriesConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex that fully qualified class names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public RepositoriesConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithRepository(true)))
        .should().haveNameMatching(regex)
        .as("Repositories should have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Adds a rule enforcing that classes with names ending in {@code Repository}
   * should be annotated with {@code @Repository}. Interfaces, such as Spring Data
   * repositories, are excluded.
   *
   * @return this configurer for fluent chaining
   */
  public RepositoriesConfigurer shouldBeAnnotatedWithRepository() {
    return shouldBeAnnotatedWithRepository(
        ClassCriteria.nameMatching(DEFAULT_REPOSITORY_NAME_MATCHING), defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithRepository()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public RepositoriesConfigurer shouldBeAnnotatedWithRepository(Configuration configuration) {
    return shouldBeAnnotatedWithRepository(
        ClassCriteria.nameMatching(DEFAULT_REPOSITORY_NAME_MATCHING), configuration);
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * should be annotated with {@code @Repository}. Interfaces, such as Spring Data
   * repositories, are excluded.
   *
   * @param classes the repository classes, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public RepositoriesConfigurer shouldBeAnnotatedWithRepository(DescribedPredicate<? super JavaClass> classes) {
    return shouldBeAnnotatedWithRepository(classes, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithRepository(DescribedPredicate)}, but with {@link Configuration} for customization.
   *
   * @param classes the repository classes
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public RepositoriesConfigurer shouldBeAnnotatedWithRepository(DescribedPredicate<? super JavaClass> classes, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areNotInterfaces().and(classes)
            .should(be(annotatedWithRepository(true)))
            .as("Repositories %s should be annotated with %s".formatted(
                classes.getDescription(), ANNOTATION_REPOSITORY)),
        configuration));
  }

  /**
   * Adds a rule enforcing that repository classes should not depend on controller classes
   * (annotated with {@code @Controller} or {@code @RestController}).
   *
   * @return this configurer for fluent chaining
   */
  public RepositoriesConfigurer shouldNotDependOnControllers() {
    return shouldNotDependOnControllers(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotDependOnControllers()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public RepositoriesConfigurer shouldNotDependOnControllers(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithRepository(true)))
            .should(not(dependOnClassesThat(annotatedWithControllerOrRestController(true))))
            .as("Repositories should not depend on Controllers or RestControllers"),
        configuration));
  }

  /**
   * Adds a rule enforcing that repository classes should not depend on service-layer classes
   * (annotated with {@code @Service}).
   *
   * @return this configurer for fluent chaining
   */
  public RepositoriesConfigurer shouldNotDependOnServices() {
    return shouldNotDependOnServices(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotDependOnServices()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public RepositoriesConfigurer shouldNotDependOnServices(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithRepository(true)))
            .should(not(dependOnClassesThat(annotatedWithService(true))))
            .as("Repositories should not depend on Services"),
        configuration));
  }

  /**
   * Adds a rule enforcing that repositories should have package-private visibility.
   * Repositories are classes annotated with {@code @Repository} or types extending
   * Spring Data's {@code org.springframework.data.repository.Repository}.
   *
   * @return this configurer for fluent chaining
   */
  public RepositoriesConfigurer shouldBePackagePrivate() {
    return shouldBePackagePrivate(defaultConfiguration());
  }

  /**
   * See {@link #shouldBePackagePrivate()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public RepositoriesConfigurer shouldBePackagePrivate(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithRepository(true).<JavaClass>forSubtype().or(springDataRepository())))
        .should().bePackagePrivate()
        .as("Repositories should be package-private"), configuration));
  }

  @Override
  public RepositoriesConfigurer disable() {
    disable(RepositoriesConfigurer.class);

    return this;
  }
}
