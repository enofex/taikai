package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.ReadOnlyTransactionalMethods.beReadOnly;
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
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;

/**
 * Configures and enforces conventions for transactional code annotated with
 * {@code @Transactional} using {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that transaction boundaries are effective, reside in the correct
 * layer and are read-only where intended. Spring's default proxy-based transaction management
 * only intercepts calls to public methods, so {@code @Transactional} on non-public methods is
 * silently ignored.</p>
 *
 * <p>Both {@code org.springframework.transaction.annotation.Transactional} and
 * {@code jakarta.transaction.Transactional} are taken into account.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .spring(spring -> spring
 *         .transactional(transactional -> transactional
 *             .methodsShouldBePublic()
 *             .methodsShouldBeReadOnly(MethodCriteria.nameMatching("(find|get|count).*"))
 *             .shouldNotBeSelfInvoked()
 *             .shouldNotBeUsedInControllers()
 *         )
 *     );
 * }</pre>
 */
public final class TransactionalConfigurer extends AbstractConfigurer implements
    DisableableConfigurer {

  TransactionalConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule that methods annotated with {@code @Transactional} must be public.
   *
   * <p>Spring's default proxy-based transaction management only applies to public methods.
   * A {@code @Transactional} annotation on a non-public method is silently ignored and no
   * transaction is created at runtime.</p>
   *
   * @return this configurer for fluent chaining
   */
  public TransactionalConfigurer methodsShouldBePublic() {
    return methodsShouldBePublic(defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldBePublic()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public TransactionalConfigurer methodsShouldBePublic(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that(are(annotatedWithTransactional(true)))
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().bePublic()
        .as(("Methods annotated with %s or %s should be public, Spring's proxy-based transaction "
            + "management ignores non-public methods").formatted(
            ANNOTATION_TRANSACTIONAL, ANNOTATION_JAKARTA_TRANSACTIONAL)), configuration));
  }

  /**
   * Adds a rule that transactional methods matching the given criteria must be read-only, as with
   * {@code @Transactional(readOnly = true)} on query methods.
   *
   * <p>Each method is checked with the transaction attribute that Spring itself derives for it,
   * with spring-tx's {@code AnnotationTransactionAttributeSource} as for a call through a
   * class-based proxy, the default in Spring Boot. All of Spring's rules apply: the annotation on
   * the method or on a method it overrides comes first, otherwise the one on its class, a
   * superclass or an interface, and composed annotations and {@code @AliasFor} are resolved. An
   * annotation on a method replaces the one on its class entirely, so a method annotated with a
   * plain {@code @Transactional} is not read-only even if its class is annotated with
   * {@code @Transactional(readOnly = true)}. {@code jakarta.transaction.Transactional} has no
   * {@code readOnly} attribute, so the methods it applies to are reported.</p>
   *
   * <p>Methods that do not run in a transaction are not checked, nor are private and static
   * methods, which Spring's proxies never intercept. The checked classes are loaded to resolve
   * their transactions, and a method whose transaction cannot be resolved is reported. The rule
   * checks the transaction that Spring declares for a method, while self-invocations, see
   * {@link #shouldNotBeSelfInvoked()}, and calls to final methods bypass the proxy. Requires
   * spring-tx and spring-context on the classpath, which Taikai declares as optional
   * dependencies.</p>
   *
   * @param methods the methods to check, selected with
   *     {@link com.enofex.taikai.MethodCriteria}, for example
   *     {@code MethodCriteria.nameMatching("(find|get|count).*")}, or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @throws IllegalStateException if spring-tx or spring-context is not on the classpath
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public TransactionalConfigurer methodsShouldBeReadOnly(
      DescribedPredicate<? super JavaMethod> methods) {
    return methodsShouldBeReadOnly(methods, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldBeReadOnly(DescribedPredicate)}, but with {@link Configuration} for
   * customization.
   *
   * @param methods the methods to check
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @throws IllegalStateException if spring-tx or spring-context is not on the classpath
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public TransactionalConfigurer methodsShouldBeReadOnly(
      DescribedPredicate<? super JavaMethod> methods, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that(methods)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .and().doNotHaveModifier(JavaModifier.PRIVATE)
        .and().doNotHaveModifier(JavaModifier.STATIC)
        .should(beReadOnly())
        .as("Transactional methods %s should be read-only".formatted(
            methods.getDescription())), configuration));
  }

  /**
   * Adds a rule that methods annotated with {@code @Transactional} must not be invoked from within
   * the class that declares them.
   *
   * <p>Spring applies transaction management through a proxy that wraps the bean. A call such as
   * {@code this.save()} targets the bean instance directly instead of the proxy, so the
   * {@code @Transactional} annotation on the called method is silently ignored and no transaction
   * is created. The transaction boundary has to be crossed through another bean.</p>
   *
   * <p>Only annotations declared on the called method are taken into account. Since the receiver
   * of a call cannot be determined statically, calls on another instance of the same class, such
   * as the self-injection workaround, are reported as well. Use {@link Configuration} to exclude
   * those classes.</p>
   *
   * @return this configurer for fluent chaining
   */
  public TransactionalConfigurer shouldNotBeSelfInvoked() {
    return shouldNotBeSelfInvoked(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotBeSelfInvoked()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public TransactionalConfigurer shouldNotBeSelfInvoked(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(notSelfInvokeMethodsAnnotatedWith(ANNOTATIONS_TRANSACTIONAL))
        .as(("Methods annotated with %s or %s should not be self-invoked, the call bypasses "
            + "Spring's proxy and no transaction is created").formatted(
            ANNOTATION_TRANSACTIONAL, ANNOTATION_JAKARTA_TRANSACTIONAL)), configuration));
  }

  /**
   * Adds a rule that controllers annotated with {@code @Controller} or {@code @RestController} must
   * not be annotated with {@code @Transactional} and must not declare {@code @Transactional}
   * methods. Transaction boundaries should be defined in the service layer.
   *
   * @return this configurer for fluent chaining
   */
  public TransactionalConfigurer shouldNotBeUsedInControllers() {
    return shouldNotBeUsedInControllers(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotBeUsedInControllers()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public TransactionalConfigurer shouldNotBeUsedInControllers(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithControllerOrRestController(true)))
        .should(notBeTransactional())
        .as(("Controllers should not use %s or %s, transaction boundaries should be defined in "
            + "the service layer").formatted(
            ANNOTATION_TRANSACTIONAL, ANNOTATION_JAKARTA_TRANSACTIONAL)), configuration));
  }

  @Override
  public TransactionalConfigurer disable() {
    disable(TransactionalConfigurer.class);

    return this;
  }
}
