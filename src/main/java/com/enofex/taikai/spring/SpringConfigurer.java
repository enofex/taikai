package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_AUTOWIRED;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithAutowired;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.configures.DisableableConfigurer;

/**
 * Top-level configurer for Spring Framework architectural rules using
 * {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer groups sub-configurers for each Spring layer — properties, configurations,
 * controllers, services, repositories, transactional code, and Spring Boot — and also enforces
 * project-wide Spring-specific rules such as prohibiting {@code @Autowired} field injection.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .spring(spring -> spring
 *         .noAutowiredFields()
 *         .boot(boot -> boot
 *             .applicationClassShouldResideInPackage("com.example.project"))
 *         .controllers(ctrl -> ctrl
 *             .namesShouldEndWithController()
 *             .shouldBeAnnotatedWithRestController()
 *             .shouldNotDependOnOtherControllers())
 *         .services(svc -> svc
 *             .namesShouldEndWithService()
 *             .shouldBeAnnotatedWithService()
 *             .shouldNotDependOnControllers())
 *         .repositories(repo -> repo
 *             .namesShouldEndWithRepository()
 *             .shouldBeAnnotatedWithRepository()
 *             .shouldNotDependOnServices())
 *     )
 *     .build()
 *     .check();
 * }</pre>
 */
public final class SpringConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  public SpringConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Configures {@code @ConfigurationProperties} rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link PropertiesConfigurer}
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer properties(Customizer<PropertiesConfigurer> customizer) {
    return customizer(customizer, () -> new PropertiesConfigurer(configurerContext()));
  }

  /**
   * Configures {@code @Configuration} class rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link ConfigurationsConfigurer}
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer configurations(
      Customizer<ConfigurationsConfigurer> customizer) {
    return customizer(customizer, () -> new ConfigurationsConfigurer(configurerContext()));
  }

  /**
   * Configures {@code @Controller} and {@code @RestController} class rules
   * using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link ControllersConfigurer}
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer controllers(
      Customizer<ControllersConfigurer> customizer) {
    return customizer(customizer, () -> new ControllersConfigurer(configurerContext()));
  }

  /**
   * Configures {@code @Service} class rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link ServicesConfigurer}
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer services(Customizer<ServicesConfigurer> customizer) {
    return customizer(customizer, () -> new ServicesConfigurer(configurerContext()));
  }

  /**
   * Configures {@code @Repository} class rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link RepositoriesConfigurer}
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer repositories(
      Customizer<RepositoriesConfigurer> customizer) {
    return customizer(customizer, () -> new RepositoriesConfigurer(configurerContext()));
  }

  /**
   * Configures Spring Boot application class rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link BootConfigurer}
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer boot(Customizer<BootConfigurer> customizer) {
    return customizer(customizer, () -> new BootConfigurer(configurerContext()));
  }

  /**
   * Configures {@code @Transactional} rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link TransactionalConfigurer}
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer transactional(
      Customizer<TransactionalConfigurer> customizer) {
    return customizer(customizer, () -> new TransactionalConfigurer(configurerContext()));
  }

  /**
   * Adds a rule that no fields in the codebase should be annotated with {@code @Autowired}.
   * Constructor injection should be preferred instead.
   *
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer noAutowiredFields() {
    return noAutowiredFields(defaultConfiguration());
  }

  /**
   * See {@link #noAutowiredFields()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer noAutowiredFields(Configuration configuration) {
    return addRule(TaikaiRule.of(noFields()
        .should(be(annotatedWithAutowired(true)))
        .as("No fields should be annotated with %s, use constructor injection".formatted(
            ANNOTATION_AUTOWIRED)), configuration));
  }

  @Override
  public SpringConfigurer disable() {
    disable(SpringConfigurer.class);
    disable(PropertiesConfigurer.class);
    disable(ConfigurationsConfigurer.class);
    disable(ControllersConfigurer.class);
    disable(ServicesConfigurer.class);
    disable(RepositoriesConfigurer.class);
    disable(BootConfigurer.class);
    disable(TransactionalConfigurer.class);

    return this;
  }
}