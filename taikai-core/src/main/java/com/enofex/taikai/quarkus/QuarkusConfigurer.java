package com.enofex.taikai.quarkus;

import static com.enofex.taikai.core.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.internal.JakartaAnnotations.ANNOTATION_INJECT;
import static com.enofex.taikai.quarkus.QuarkusDescribedPredicates.annotatedWithInject;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.configures.DisableableConfigurer;
import com.enofex.taikai.core.TaikaiRule;
import com.enofex.taikai.core.TaikaiRule.Configuration;

/**
 * Rules for Quarkus applications, with groups for REST resources, Panache and AI services:
 *
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
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
 *             .namesShouldEndWithAssistantOrService()
 *             .shouldBeAnnotatedWithApplicationScoped()
 *             .shouldNotUseToolsAttributeInAiService())
 *     )
 *     .build()
 *     .check();
 * }</pre>
 */
public final class QuarkusConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  /**
   * @param configurerContext the context of the builder
   */
  public QuarkusConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule that fields in the codebase must not be annotated with {@code @Inject}. Constructor
   * injection should be preferred instead.
   *
   * @return this configurer for fluent chaining
   */
  public QuarkusConfigurer noInjectionFields() {
    return noInjectionFields(defaultConfiguration());
  }

  /**
   * See {@link #noInjectionFields()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public QuarkusConfigurer noInjectionFields(Configuration configuration) {
    return addRule(TaikaiRule.of(noFields()
        .should(be(annotatedWithInject(true)))
        .as("No fields should be annotated with %s, use constructor injection".formatted(
            ANNOTATION_INJECT)), configuration));
  }

  /**
   * Configures JAX-RS {@code @Path} resource class rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link ResourcesConfigurer}
   * @return this configurer for fluent chaining
   */
  public QuarkusConfigurer resources(
      Customizer<ResourcesConfigurer> customizer) {
    return customizer(customizer, () -> new ResourcesConfigurer(configurerContext()));
  }

  /**
   * Configures Panache entity and repository rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link PanacheConfigurer}
   * @return this configurer for fluent chaining
   */
  public QuarkusConfigurer panache(
      Customizer<PanacheConfigurer> customizer) {
    return customizer(customizer, () -> new PanacheConfigurer(configurerContext()));
  }

  /**
   * Configures LangChain4j AI service rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link AiConfigurer}
   * @return this configurer for fluent chaining
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
