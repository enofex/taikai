package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SelfInvokedProxiedMethods.notSelfInvokeMethodsAnnotatedWith;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATIONS_TRANSACTIONAL;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_JAKARTA_TRANSACTIONAL;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_TRANSACTIONAL;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithControllerOrRestController;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithTransactional;
import static com.enofex.taikai.spring.TransactionalControllers.notBeTransactional;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;

/**
 * Configures and enforces conventions for transactional code annotated with
 * {@code @Transactional} using {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that transaction boundaries are effective and reside in the
 * correct layer. Spring's default proxy-based transaction management only intercepts calls
 * to public methods, so {@code @Transactional} on non-public methods is silently ignored.</p>
 *
 * <p>Both {@code org.springframework.transaction.annotation.Transactional} and
 * {@code jakarta.transaction.Transactional} are taken into account.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .spring(spring -> spring
 *         .transactional(transactional -> transactional
 *             .methodsShouldBePublic()
 *             .shouldNotBeSelfInvoked()
 *             .shouldNotBeUsedInControllers()
 *         )
 *     );
 * }</pre>
 */
public final class TransactionalConfigurer extends AbstractConfigurer implements
    DisableableConfigurer {

  public TransactionalConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule enforcing that methods annotated with {@code @Transactional} are public.
   *
   * <p>Spring's default proxy-based transaction management only applies to public methods.
   * A {@code @Transactional} annotation on a non-public method is silently ignored and no
   * transaction is created at runtime.</p>
   *
   * @return this configurer instance for fluent chaining
   */
  public TransactionalConfigurer methodsShouldBePublic() {
    return methodsShouldBePublic(defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldBePublic()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public TransactionalConfigurer methodsShouldBePublic(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that(are(annotatedWithTransactional(true)))
        .should().bePublic()
        .as("Methods annotated with %s or %s should be public, Spring's proxy-based transaction management ignores non-public methods".formatted(
            ANNOTATION_TRANSACTIONAL, ANNOTATION_JAKARTA_TRANSACTIONAL)), configuration));
  }

  /**
   * Adds a rule enforcing that methods annotated with {@code @Transactional} are not invoked from
   * within the class that declares them.
   *
   * <p>Spring applies transaction management through a proxy that wraps the bean. A call such as
   * {@code this.save()} targets the bean instance directly instead of the proxy, so the
   * {@code @Transactional} annotation on the called method is silently ignored and no transaction
   * is created. The transaction boundary has to be crossed through another bean.</p>
   *
   * <p>Only annotations declared on the called method are taken into account. Since the receiver
   * of a call cannot be determined statically, calls on another instance of the same class, such
   * as the self injection workaround, are reported as well. Use {@link Configuration} to exclude
   * those classes.</p>
   *
   * @return this configurer instance for fluent chaining
   */
  public TransactionalConfigurer shouldNotBeSelfInvoked() {
    return shouldNotBeSelfInvoked(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotBeSelfInvoked()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public TransactionalConfigurer shouldNotBeSelfInvoked(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(notSelfInvokeMethodsAnnotatedWith(ANNOTATIONS_TRANSACTIONAL))
        .as("Methods annotated with %s or %s should not be self invoked, the call bypasses Spring's proxy and no transaction is created".formatted(
            ANNOTATION_TRANSACTIONAL, ANNOTATION_JAKARTA_TRANSACTIONAL)), configuration));
  }

  /**
   * Adds a rule enforcing that controllers annotated with {@code @Controller} or
   * {@code @RestController} are not annotated with {@code @Transactional} and do not
   * declare {@code @Transactional} methods. Transaction boundaries should be defined
   * in the service layer.
   *
   * @return this configurer instance for fluent chaining
   */
  public TransactionalConfigurer shouldNotBeUsedInControllers() {
    return shouldNotBeUsedInControllers(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotBeUsedInControllers()}, but with {@link Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public TransactionalConfigurer shouldNotBeUsedInControllers(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithControllerOrRestController(true)))
        .should(notBeTransactional())
        .as("Controllers should not use %s or %s, transaction boundaries should be defined in the service layer".formatted(
            ANNOTATION_TRANSACTIONAL, ANNOTATION_JAKARTA_TRANSACTIONAL)), configuration));
  }

  @Override
  public TransactionalConfigurer disable() {
    disable(TransactionalConfigurer.class);

    return this;
  }
}
