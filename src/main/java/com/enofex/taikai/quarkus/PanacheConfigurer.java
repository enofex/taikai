package com.enofex.taikai.quarkus;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.quarkus.QuarkusDescribedPredicates.ANNOTATION_ENTITY;
import static com.enofex.taikai.quarkus.QuarkusDescribedPredicates.PANACHE_ENTITY;
import static com.enofex.taikai.quarkus.QuarkusDescribedPredicates.annotatedWithEntity;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Configures and enforces conventions for Quarkus Panache entities and repositories
 * using {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that Panache Active Record entities are annotated with
 * {@code @Entity}, and that Panache Repository implementations follow consistent naming
 * conventions.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .quarkus(quarkus -> quarkus
 *         .panache(panache -> panache
 *             .shouldBeAnnotatedWithEntityWhenActiveRecordPattern()
 *             .namesShouldEndWithRepository()
 *             .namesShouldMatch(".*Repository")
 *         )
 *     )
 *     .build()
 *     .check();
 * }</pre>
 *
 * <p>By default, this configurer checks that classes extending {@code PanacheEntity}
 * are annotated with {@code @Entity}, and that classes implementing {@code PanacheRepository}
 * have names ending with {@code Repository}.</p>
 */
public class PanacheConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  private static final String DEFAULT_REPOSITORY_NAME_MATCHING = ".+Repository";

  public PanacheConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule enforcing that classes implementing {@code PanacheEntity}
   * are annotated with {@code Entity}.
   *
   * @return this configurer instance for fluent chaining
   */
  public PanacheConfigurer shouldBeAnnotatedWithEntityWhenActiveRecordPattern() {
    return annotatedWithEntityWhenActiveRecordPattern(defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithEntityWhenActiveRecordPattern()}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public PanacheConfigurer shouldBeAnnotatedWithEntityWhenActiveRecordPattern(TaikaiRule.Configuration configuration) {
    return annotatedWithEntityWhenActiveRecordPattern(configuration);
  }

  /**
   * Adds a rule enforcing that classes implementing {@code PanacheEntity}
   * are annotated with {@code Entity}.
   *
   * @return this configurer instance for fluent chaining
   */
  public PanacheConfigurer annotatedWithEntityWhenActiveRecordPattern() {
    return annotatedWithEntityWhenActiveRecordPattern(defaultConfiguration());
  }

  /**
   * See {@link #annotatedWithEntityWhenActiveRecordPattern()}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public PanacheConfigurer annotatedWithEntityWhenActiveRecordPattern(TaikaiRule.Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areAssignableTo(PANACHE_ENTITY)
            .should(be(annotatedWithEntity(true)))
            .as("Active Record Pattern entities should be annotated with %s"
                .formatted(ANNOTATION_ENTITY)),
        configuration));
  }

  /**
   * Adds a rule enforcing that classes implementing {@code PanacheRepository}
   * have names ending with {@code Repository}.
   *
   * @return this configurer instance for fluent chaining
   */
  public PanacheConfigurer namesShouldEndWithRepository() {
    return namesShouldMatch(DEFAULT_REPOSITORY_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldEndWithRepository()}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public PanacheConfigurer namesShouldEndWithRepository(TaikaiRule.Configuration configuration) {
    return namesShouldMatch(DEFAULT_REPOSITORY_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that classes implementing {@code PanacheRepository}
   * have names matching the given regex.
   *
   * @param regex the regex pattern for valid repository class names
   * @return this configurer instance for fluent chaining
   */
  public PanacheConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param regex         the regex pattern for valid repository class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public PanacheConfigurer namesShouldMatch(String regex, TaikaiRule.Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().implement(QuarkusDescribedPredicates.PANACHE_REPOSITORY_INTERFACE)
        .should().haveNameMatching(regex)
        .as("Repositories should have names matching %s".formatted(regex)), configuration));
  }

  @Override
  public PanacheConfigurer disable() {
    disable(PanacheConfigurer.class);
    return this;
  }
}
