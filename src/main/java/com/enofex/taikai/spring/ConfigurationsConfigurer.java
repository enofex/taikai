package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithSpringBootApplication;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;

/**
 * Configures and enforces conventions for Spring {@code @Configuration} classes using
 * {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that Spring configuration classes have consistent naming
 * and do not conflict with {@code @SpringBootApplication} classes.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .spring(spring -> spring
 *         .configurations(config -> config
 *             .namesShouldEndWithConfiguration()
 *         )
 *     );
 * }</pre>
 *
 * <p>By default, this enforces that all classes annotated with {@code @Configuration}
 * (but not {@code @SpringBootApplication}) end with the suffix {@code Configuration}.</p>
 */
public final class ConfigurationsConfigurer extends AbstractConfigurer implements
    DisableableConfigurer {

  private static final String DEFAULT_CONFIGURATION_NAME_MATCHING = ".+Configuration";

  public ConfigurationsConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule that ensures all Spring {@code @Configuration}-annotated classes (excluding
   * {@code @SpringBootApplication} classes) have names ending with {@code Configuration}.
   *
   * @return this configurer instance for fluent chaining
   */
  public ConfigurationsConfigurer namesShouldEndWithConfiguration() {
    return namesShouldMatch(DEFAULT_CONFIGURATION_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldEndWithConfiguration()}, but with {@link Configuration} for
   * customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ConfigurationsConfigurer namesShouldEndWithConfiguration(Configuration configuration) {
    return namesShouldMatch(DEFAULT_CONFIGURATION_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule that ensures all Spring {@code @Configuration}-annotated classes (excluding
   * {@code @SpringBootApplication} classes) have names matching the given regex.
   *
   * @param regex the regex pattern for valid configuration class names
   * @return this configurer instance for fluent chaining
   */
  public ConfigurationsConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex         the regex pattern for valid configuration class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public ConfigurationsConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithConfiguration(true)
            .and(not(annotatedWithSpringBootApplication(true))))
        )
        .should().haveNameMatching(regex)
        .as("Configurations should have name ending %s".formatted(regex)), configuration));
  }

  @Override
  public ConfigurationsConfigurer disable() {
    disable(ConfigurationsConfigurer.class);

    return this;
  }
}
