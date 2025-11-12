package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_CONFIGURATION_PROPERTIES;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_VALIDATED;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithConfigurationProperties;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;

/**
 * Configures and enforces conventions for Spring {@code @ConfigurationProperties} classes
 * using {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that configuration property classes follow consistent naming,
 * and that they are properly annotated with {@code @Validated} when needed.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .spring(spring -> spring
 *         .properties(props -> props
 *             .namesShouldEndWithProperties()
 *             .shouldBeAnnotatedWithValidated()
 *             .shouldBeAnnotatedWithConfigurationProperties()
 *             .shouldBeRecords()
 *         )
 *     );
 * }</pre>
 *
 * <p>By default, this configurer applies to classes annotated with {@code @ConfigurationProperties}.</p>
 */
public final class PropertiesConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  private static final String DEFAULT_PROPERTIES_NAME_MATCHING = ".+Properties";

  PropertiesConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule enforcing that classes annotated with {@code @ConfigurationProperties}
   * have names ending with {@code Properties}.
   *
   * @return this configurer instance for fluent chaining
   */
  public PropertiesConfigurer namesShouldEndWithProperties() {
    return namesShouldMatch(DEFAULT_PROPERTIES_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldEndWithProperties()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public PropertiesConfigurer namesShouldEndWithProperties(Configuration configuration) {
    return namesShouldMatch(DEFAULT_PROPERTIES_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that {@code @ConfigurationProperties}-annotated classes
   * have names matching the given regex.
   *
   * @param regex the regex pattern for valid property class names
   * @return this configurer instance for fluent chaining
   */
  public PropertiesConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex pattern for valid property class names
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public PropertiesConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithConfigurationProperties(true)))
        .should().haveNameMatching(regex)
        .as("Properties should have name ending %s".formatted(regex)), configuration));
  }

  /**
   * Adds a rule enforcing that classes annotated with {@code @ConfigurationProperties}
   * are also annotated with {@code @Validated}.
   *
   * @return this configurer instance for fluent chaining
   */
  public PropertiesConfigurer shouldBeAnnotatedWithValidated() {
    return shouldBeAnnotatedWithValidated(defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithValidated()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public PropertiesConfigurer shouldBeAnnotatedWithValidated(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithConfigurationProperties(true)))
            .should().beMetaAnnotatedWith(ANNOTATION_VALIDATED)
            .as("Configuration properties annotated with %s should be annotated with %s as well".formatted(
                ANNOTATION_CONFIGURATION_PROPERTIES, ANNOTATION_VALIDATED)),
        configuration));
  }

  /**
   * Adds a rule enforcing that classes (by default ending with {@code Properties})
   * should be annotated with {@code @ConfigurationProperties}.
   *
   * @return this configurer instance for fluent chaining
   */
  public PropertiesConfigurer shouldBeAnnotatedWithConfigurationProperties() {
    return shouldBeAnnotatedWithConfigurationProperties(DEFAULT_PROPERTIES_NAME_MATCHING,
        defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithConfigurationProperties()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public PropertiesConfigurer shouldBeAnnotatedWithConfigurationProperties(Configuration configuration) {
    return shouldBeAnnotatedWithConfigurationProperties(DEFAULT_PROPERTIES_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that classes with names matching the given regex
   * should be annotated with {@code @ConfigurationProperties}.
   *
   * @param regex the regex for class names expected to be configuration properties
   * @return this configurer instance for fluent chaining
   */
  public PropertiesConfigurer shouldBeAnnotatedWithConfigurationProperties(String regex) {
    return shouldBeAnnotatedWithConfigurationProperties(regex, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithConfigurationProperties(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex for class names expected to be configuration properties
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public PropertiesConfigurer shouldBeAnnotatedWithConfigurationProperties(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(be(annotatedWithConfigurationProperties(true)))
            .as("Configuration properties should be annotated with %s".formatted(
                ANNOTATION_CONFIGURATION_PROPERTIES)),
        configuration));
  }

  /**
   * Adds a rule enforcing that all {@code @ConfigurationProperties}-annotated classes
   * should be implemented as Java {@code record}s.
   *
   * <p>This promotes immutability, thread-safety, and consistency in configuration design.</p>
   *
   * @return this configurer instance for fluent chaining
   */
  public PropertiesConfigurer shouldBeRecords() {
    return shouldBeRecords(defaultConfiguration());
  }

  /**
   * See {@link #shouldBeRecords()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public PropertiesConfigurer shouldBeRecords(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithConfigurationProperties(true)))
            .should().beRecords()
            .as("Configuration properties annotated with %s should be records"
                .formatted(ANNOTATION_CONFIGURATION_PROPERTIES)),
        configuration));
  }


  @Override
  public PropertiesConfigurer disable() {
    disable(PropertiesConfigurer.class);

    return this;
  }
}
