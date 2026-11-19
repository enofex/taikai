package com.enofex.taikai.spring;

import static com.enofex.taikai.core.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithSpringBootApplication;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;
import com.enofex.taikai.core.TaikaiRule;
import com.enofex.taikai.core.TaikaiRule.Configuration;

/**
 * Rules for Spring configuration classes, the classes annotated with {@code @Configuration} but not
 * with {@code @SpringBootApplication}:
 *
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .spring(spring -> spring
 *         .configurations(config -> config
 *             .namesShouldEndWithConfiguration()
 *         )
 *     );
 * }</pre>
 */
public final class ConfigurationsConfigurer extends AbstractConfigurer implements
    DisableableConfigurer {

  private static final String DEFAULT_CONFIGURATION_NAME_MATCHING = ".+Configuration";

  ConfigurationsConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule that all Spring classes annotated with {@code @Configuration} (excluding
   * {@code @SpringBootApplication} classes) must have names ending with {@code Configuration}.
   *
   * @return this configurer for fluent chaining
   */
  public ConfigurationsConfigurer namesShouldEndWithConfiguration() {
    return namesShouldMatch(DEFAULT_CONFIGURATION_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldEndWithConfiguration()}, but with {@link Configuration} for
   * customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ConfigurationsConfigurer namesShouldEndWithConfiguration(Configuration configuration) {
    return namesShouldMatch(DEFAULT_CONFIGURATION_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule that all Spring classes annotated with {@code @Configuration} (excluding
   * {@code @SpringBootApplication} classes) must have names matching the given regex.
   *
   * @param regex the regex that fully qualified class names must match
   * @return this configurer for fluent chaining
   */
  public ConfigurationsConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex that fully qualified class names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ConfigurationsConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithConfiguration(true)
            .and(not(annotatedWithSpringBootApplication(true))))
        )
        .should().haveNameMatching(regex)
        .as("Configurations should have names matching %s".formatted(regex)), configuration));
  }

  @Override
  public ConfigurationsConfigurer disable() {
    disable(ConfigurationsConfigurer.class);

    return this;
  }
}
