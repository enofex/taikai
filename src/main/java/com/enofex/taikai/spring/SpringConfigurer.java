package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SelfInvokedProxiedMethods.notSelfInvokeMethodsAnnotatedWith;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATIONS_APPLIED_BY_PROXY;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_AUTOWIRED;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithAutowired;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.configures.DisableableConfigurer;
import java.util.Collection;

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
 *         .noSelfInvocationOfProxiedMethods()
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

  /**
   * Adds a rule that methods carrying an annotation which Spring applies through a proxy are not
   * invoked from within the class that declares them.
   *
   * <p>Spring implements {@code @Transactional}, {@code @Async}, caching, method security and
   * retries by wrapping the bean in a proxy. A call such as {@code this.send()} targets the bean
   * instance directly instead of the proxy, so the annotation on the called method is silently
   * ignored, no transaction is started, no thread is switched, no cache is consulted and no
   * security check is performed. The proxy is only involved when the call comes from another
   * bean.</p>
   *
   * <p>By default the following annotations are checked, none of them has to be on the
   * classpath:</p>
   * <ul>
   *   <li>{@code org.springframework.transaction.annotation.Transactional}</li>
   *   <li>{@code jakarta.transaction.Transactional}</li>
   *   <li>{@code org.springframework.scheduling.annotation.Async}</li>
   *   <li>{@code org.springframework.cache.annotation.Cacheable}</li>
   *   <li>{@code org.springframework.cache.annotation.CacheEvict}</li>
   *   <li>{@code org.springframework.cache.annotation.CachePut}</li>
   *   <li>{@code org.springframework.security.access.prepost.PreAuthorize}</li>
   *   <li>{@code org.springframework.security.access.prepost.PostAuthorize}</li>
   *   <li>{@code org.springframework.retry.annotation.Retryable}</li>
   * </ul>
   *
   * <p>Only annotations declared on the called method are taken into account. Since the receiver
   * of a call cannot be determined statically, calls on another instance of the same class, such
   * as the self injection workaround, are reported as well. Use {@link Configuration} to exclude
   * those classes.</p>
   *
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer noSelfInvocationOfProxiedMethods() {
    return noSelfInvocationOfProxiedMethods(ANNOTATIONS_APPLIED_BY_PROXY, defaultConfiguration());
  }

  /**
   * See {@link #noSelfInvocationOfProxiedMethods()}, but with {@link Configuration} for
   * customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer noSelfInvocationOfProxiedMethods(Configuration configuration) {
    return noSelfInvocationOfProxiedMethods(ANNOTATIONS_APPLIED_BY_PROXY, configuration);
  }

  /**
   * See {@link #noSelfInvocationOfProxiedMethods()}, but with a custom set of annotations instead
   * of the default ones.
   *
   * @param annotations the fully qualified names of the annotations to check
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer noSelfInvocationOfProxiedMethods(Collection<String> annotations) {
    return noSelfInvocationOfProxiedMethods(annotations, defaultConfiguration());
  }

  /**
   * See {@link #noSelfInvocationOfProxiedMethods(Collection)}, but with {@link Configuration} for
   * customization.
   *
   * @param annotations   the fully qualified names of the annotations to check
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public SpringConfigurer noSelfInvocationOfProxiedMethods(Collection<String> annotations,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(notSelfInvokeMethodsAnnotatedWith(annotations))
        .as("Methods annotated with %s should not be self invoked, the call bypasses Spring's proxy and the annotation has no effect".formatted(
            annotations)), configuration));
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