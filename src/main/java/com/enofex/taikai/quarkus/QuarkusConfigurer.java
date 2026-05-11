package com.enofex.taikai.quarkus;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.configures.DisableableConfigurer;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.quarkus.QuarkusDescribedPredicates.annotatedWithInject;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * Top-level configurer for Quarkus architectural rules using
 * {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer groups sub-configurers for each Quarkus layer — REST resources,
 * Panache entities and repositories, and AI services — and also enforces project-wide
 * Quarkus-specific rules such as prohibiting {@code @Inject} field injection.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .quarkus(quarkus -> quarkus
 *         .noInjectionFields()
 *         .resources(res -> res
 *             .namesShouldEndWithResource()
 *             .shouldBeAnnotatedWithPath()
 *             .shouldBePublic()
 *             .shouldNotDependOnOtherResources())
 *         .panache(panache -> panache
 *             .shouldBeAnnotatedWithEntityWhenActiveRecordPattern()
 *             .namesShouldEndWithRepository())
 *         .ai(ai -> ai
 *             .namesShouldEndWithAssistantOrResource()
 *             .shouldBeAnnotatedWithApplicationScoped()
 *             .shouldNotUseToolsAttributeInAiService())
 *     )
 *     .build()
 *     .check();
 * }</pre>
 */
public final class QuarkusConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  public QuarkusConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule that no fields in the codebase should be annotated with {@code @Inject}.
   * Constructor injection should be preferred instead.
   *
   * @return this configurer instance for fluent chaining
   */
  public QuarkusConfigurer noInjectionFields() {
    return noInjectionFields(defaultConfiguration());
  }

  /**
   * See {@link #noInjectionFields()}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param configuration the configuration for rule customization
   * @return this configurer instance for fluent chaining
   */
  public QuarkusConfigurer noInjectionFields(TaikaiRule.Configuration configuration) {
    return addRule(TaikaiRule.of(noFields()
        .should(be(annotatedWithInject(true)))
        .as("No fields should be annotated with %s, use constructor injection".formatted(
            QuarkusDescribedPredicates.ANNOTATION_INJECT)), configuration));
  }

  /**
   * Configures JAX-RS {@code @Path} resource class rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link ResourcesConfigurer}
   * @return this configurer instance for fluent chaining
   */
  public QuarkusConfigurer resources(
      Customizer<ResourcesConfigurer> customizer) {
    return customizer(customizer, () -> new ResourcesConfigurer(configurerContext()));
  }

  /**
   * Configures Panache entity and repository rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link PanacheConfigurer}
   * @return this configurer instance for fluent chaining
   */
  public QuarkusConfigurer panache(
      Customizer<PanacheConfigurer> customizer) {
    return customizer(customizer, () -> new PanacheConfigurer(configurerContext()));
  }

  /**
   * Configures LangChain4j AI service rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link AiConfigurer}
   * @return this configurer instance for fluent chaining
   */
  public QuarkusConfigurer ai(
      Customizer<AiConfigurer> customizer) {
    return customizer(customizer, () -> new AiConfigurer(configurerContext()));
  }

  @Override
  public QuarkusConfigurer disable() {
    disable(QuarkusConfigurer.class);
    disable(ResourcesConfigurer.class);
    disable(PanacheConfigurer.class);
    disable(AiConfigurer.class);

    return this;
  }
}
