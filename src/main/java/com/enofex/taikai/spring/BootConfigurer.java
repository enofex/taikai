package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_SPRING_BOOT_APPLICATION;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithSpringBootApplication;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static java.util.Objects.requireNonNull;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;

/**
 * Configures and enforces conventions for Spring Boot application classes using
 * {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that the class annotated with {@code @SpringBootApplication}
 * resides in the correct package, maintaining a clear and predictable project structure.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .spring(spring -> spring
 *         .boot(boot -> boot
 *             .applicationClassShouldResideInPackage("com.example.project")
 *         )
 *     );
 * }</pre>
 *
 * <p>By default, this rule ensures that your {@code @SpringBootApplication}-annotated class
 * is located in the defined root package or a designated boot package.</p>
 */
public final class BootConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  public BootConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule that classes annotated with {@code @SpringBootApplication} must reside in the
   * project's root package (i.e., the namespace configured via
   * {@link com.enofex.taikai.Taikai.Builder#namespace(String)}).
   *
   * <p>This variant uses the namespace provided in the Taikai configuration
   * and applies the rule without requiring an explicit package argument.</p>
   *
   * @return this configurer instance for fluent chaining
   * @throws NullPointerException if no namespace is configured
   */
  public BootConfigurer applicationClassShouldResideInPackage() {
    String namespace = configurerContext().namespace();
    requireNonNull(namespace, "Namespace must not be null");

    return applicationClassShouldResideInPackage(namespace, defaultConfiguration());
  }

  /**
   * @deprecated Use {@link #applicationClassShouldResideInPackage(String)} instead.
   */
  @Deprecated(forRemoval = true)
  public BootConfigurer springBootApplicationShouldBeIn(String packageIdentifier) {
    return applicationClassShouldResideInPackage(packageIdentifier);
  }

  /**
   * @deprecated Use {@link #applicationClassShouldResideInPackage(String, Configuration)}
   * instead.
   */
  @Deprecated(forRemoval = true)
  public BootConfigurer springBootApplicationShouldBeIn(String packageIdentifier,
      Configuration configuration) {
    return applicationClassShouldResideInPackage(packageIdentifier, configuration);
  }

  /**
   * Adds a rule that classes annotated with {@code @SpringBootApplication} should reside in the
   * specified package.
   *
   * @param packageIdentifier the target package identifier (e.g., {@code "com.example.project"})
   * @return this configurer instance for fluent chaining
   */
  public BootConfigurer applicationClassShouldResideInPackage(String packageIdentifier) {
    requireNonNull(packageIdentifier);
    return applicationClassShouldResideInPackage(packageIdentifier, defaultConfiguration());
  }

  /**
   * See {@link #applicationClassShouldResideInPackage(String)}, but with {@link Configuration}
   * for customization.
   */
  public BootConfigurer applicationClassShouldResideInPackage(String packageIdentifier,
      Configuration configuration) {
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
