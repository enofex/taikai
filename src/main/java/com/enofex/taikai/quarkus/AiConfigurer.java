package com.enofex.taikai.quarkus;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.quarkus.QuarkusDescribedPredicates.APPLICATION_SCOPED;
import static com.enofex.taikai.quarkus.QuarkusDescribedPredicates.annotatedWithRegisterAiService;
import static com.enofex.taikai.quarkus.QuarkusDescribedPredicates.notUseToolsAttribute;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Configures and enforces conventions for LangChain4j AI services annotated with
 * {@code @RegisterAiService} using {@link com.tngtech.archunit ArchUnit} through the Taikai
 * framework.
 *
 * <p>This configurer ensures that AI service interfaces follow consistent naming conventions,
 * are scoped correctly with {@code @ApplicationScoped}, and do not use the deprecated
 * {@code tools} attribute of {@code @RegisterAiService} (preferring {@code @Toolbox} instead).</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .quarkus(quarkus -> quarkus
 *         .ai(ai -> ai
 *             .namesShouldEndWithAssistantOrService()
 *             .shouldBeAnnotatedWithApplicationScoped()
 *             .shouldNotUseToolsAttributeInAiService()
 *         )
 *     )
 *     .build()
 *     .check();
 * }</pre>
 *
 * <p>By default, this configurer checks classes annotated with {@code @RegisterAiService}.</p>
 */
public final class AiConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  private static final String DEFAULT_ASSISTANT_NAME_MATCHING = ".+(Assistant|Service)";

  AiConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule enforcing that an AI Service have names ending with {@code Assistant} or {@code Service}.
   *
   * @return this configurer for fluent chaining
   */
  public AiConfigurer namesShouldEndWithAssistantOrService() {
    return namesShouldMatch(DEFAULT_ASSISTANT_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldEndWithAssistantOrService()}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public AiConfigurer namesShouldEndWithAssistantOrService(TaikaiRule.Configuration configuration) {
    return namesShouldMatch(DEFAULT_ASSISTANT_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that AI Service class names match the given regex.
   *
   * @param regex the regex that fully qualified class names must match
   * @return this configurer for fluent chaining
   */
  public AiConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param regex the regex that fully qualified class names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public AiConfigurer namesShouldMatch(String regex, TaikaiRule.Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithRegisterAiService(true)))
        .should().haveNameMatching(regex)
        .as("AI Services should have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Adds a rule enforcing that classes annotated with {@code RegisterAiService}
   * are annotated with {@code ApplicationScoped}.
   *
   * @return this configurer for fluent chaining
   */
  public AiConfigurer shouldBeAnnotatedWithApplicationScoped() {
    return shouldBeAnnotatedWithApplicationScoped(defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithApplicationScoped()}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public AiConfigurer shouldBeAnnotatedWithApplicationScoped(TaikaiRule.Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithRegisterAiService(true)))
            .should(be(QuarkusDescribedPredicates.annotatedWithApplicationScope(true)))
            .as("AI Services should be annotated with %s"
                .formatted(APPLICATION_SCOPED)),
        configuration));
  }

  /**
   * Adds a rule enforcing that classes annotated with {@code RegisterAiService}
   * are not using {@code tools} attribute to define Tools.
   *
   * @return this configurer for fluent chaining
   */
  public AiConfigurer shouldNotUseToolsAttributeInAiService() {
    return shouldNotUseToolsAttributeInAiService(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotUseToolsAttributeInAiService()}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public AiConfigurer shouldNotUseToolsAttributeInAiService(TaikaiRule.Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithRegisterAiService(true)))
            .should(notUseToolsAttribute())
            .as("AI Services should not use the tools attribute to register tools, use @Toolbox"),
        configuration));
  }


  @Override
  public AiConfigurer disable() {
    disable(AiConfigurer.class);
    return this;
  }
}
