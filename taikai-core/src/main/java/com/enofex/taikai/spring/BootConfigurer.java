package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_SPRING_BOOT_APPLICATION;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithSpringBootApplication;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static java.util.Objects.requireNonNull;

import com.enofex.taikai.TaikaiException;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;
import com.enofex.taikai.internal.Namespaces;

/**
 * Rules for the Spring Boot application class, the class annotated with
 * {@code @SpringBootApplication}:
 *
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .spring(spring -> spring
 *         .boot(boot -> boot
 *             .applicationClassShouldResideInPackage("com.company.project")
 *         )
 *     );
 * }</pre>
 */
public final class BootConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  BootConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * See {@link #applicationClassShouldResideInPackage(String)}, but for the namespace configured
   * with {@link com.enofex.taikai.Taikai.Builder#namespace(String)} as the package. The namespace
   * is read when the rule is configured, so it must be set on the builder before
   * {@code spring(...)} is first configured.
   *
   * @return this configurer for fluent chaining
   * @throws TaikaiException if no namespace is configured
   */
  public BootConfigurer applicationClassShouldResideInPackage() {
    return applicationClassShouldResideInPackage(defaultConfiguration());
  }

  /**
   * See {@link #applicationClassShouldResideInPackage()}, but with {@link Configuration} for
   * customization. The namespace of the configuration is used as the root package if it is set,
   * otherwise the namespace configured via
   * {@link com.enofex.taikai.Taikai.Builder#namespace(String)}.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws TaikaiException if no namespace is configured
   */
  public BootConfigurer applicationClassShouldResideInPackage(Configuration configuration) {
    String namespace = Namespaces.require(configuration.namespace(),
        configurerContext().namespace());

    return applicationClassShouldResideInPackage(namespace, configuration);
  }

  /**
   * Adds a rule that classes annotated with {@code @SpringBootApplication} must reside in the given
   * package.
   *
   * @param packageIdentifier the target package identifier, for example
   *     {@code "com.company.project"}
   * @return this configurer for fluent chaining
   */
  public BootConfigurer applicationClassShouldResideInPackage(String packageIdentifier) {
    return applicationClassShouldResideInPackage(packageIdentifier, defaultConfiguration());
  }

  /**
   * See {@link #applicationClassShouldResideInPackage(String)}, but with {@link Configuration}
   * for customization.
   *
   * @param packageIdentifier the target package identifier, for example
   *     {@code "com.company.project"}
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public BootConfigurer applicationClassShouldResideInPackage(String packageIdentifier,
      Configuration configuration) {
    requireNonNull(packageIdentifier);

    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithSpringBootApplication(true)))
        .should().resideInAPackage(packageIdentifier)
        .allowEmptyShould(false)
        .as("Classes annotated with %s should reside in package %s".formatted(
            ANNOTATION_SPRING_BOOT_APPLICATION, packageIdentifier)), configuration));
  }

  @Override
  public BootConfigurer disable() {
    disable(BootConfigurer.class);

    return this;
  }
}
