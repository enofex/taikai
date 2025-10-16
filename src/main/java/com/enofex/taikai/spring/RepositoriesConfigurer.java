package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_REPOSITORY;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithRepository;
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
 *         )
 *     );
 * }</pre>
 *
 * <p>By default, this configurer applies to classes annotated with {@code @Repository}.</p>
 */
public class RepositoriesConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_REPOSITORY_NAME_MATCHING = ".+Repository";

  RepositoriesConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule enforcing that classes annotated with {@code @Repository}
   * have names ending with {@code Repository}.
   *
   * @return this configurer instance for fluent chaining
   */
  public RepositoriesConfigurer namesShouldEndWithRepository() {
    return namesShouldMatch(DEFAULT_REPOSITORY_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldEndWithRepository()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public RepositoriesConfigurer namesShouldEndWithRepository(Configuration configuration) {
    return namesShouldMatch(DEFAULT_REPOSITORY_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that {@code @Repository}-annotated classes
   * have names matching the given regex.
   *
   * @param regex the regex pattern for valid repository class names
   * @return this configurer instance for fluent chaining
   */
  public RepositoriesConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex pattern for valid repository class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public RepositoriesConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithRepository(true)))
        .should().haveNameMatching(regex)
        .as("Repositories should have name ending %s".formatted(regex)), configuration));
  }

  /**
   * Adds a rule enforcing that classes with names ending in {@code Repository}
   * should be annotated with {@code @Repository}.
   *
   * @return this configurer instance for fluent chaining
   */
  public RepositoriesConfigurer shouldBeAnnotatedWithRepository() {
    return shouldBeAnnotatedWithRepository(DEFAULT_REPOSITORY_NAME_MATCHING,
        defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithRepository()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public RepositoriesConfigurer shouldBeAnnotatedWithRepository(Configuration configuration) {
    return shouldBeAnnotatedWithRepository(DEFAULT_REPOSITORY_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that classes matching the given regex
   * should be annotated with {@code @Repository}.
   *
   * @param regex the regex for repository class names
   * @return this configurer instance for fluent chaining
   */
  public RepositoriesConfigurer shouldBeAnnotatedWithRepository(String regex) {
    return shouldBeAnnotatedWithRepository(regex, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithRepository(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex for repository class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public RepositoriesConfigurer shouldBeAnnotatedWithRepository(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(be(annotatedWithRepository(true)))
            .as("Repositories should be annotated with %s".formatted(ANNOTATION_REPOSITORY)),
        configuration));
  }

  /**
   * Adds a rule enforcing that repository classes should not depend on service-layer classes
   * (annotated with {@code @Service}).
   *
   * @return this configurer instance for fluent chaining
   */
  public RepositoriesConfigurer shouldNotDependOnServices() {
    return shouldNotDependOnServices(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotDependOnServices()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public RepositoriesConfigurer shouldNotDependOnServices(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithRepository(true)))
            .should(not(dependOnClassesThat(annotatedWithService(true))))
            .as("Repositories should not depend on Services"),
        configuration));
  }

  public static final class Disableable extends RepositoriesConfigurer implements
      DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public RepositoriesConfigurer disable() {
      disable(RepositoriesConfigurer.class);
      return this;
    }
  }
}
