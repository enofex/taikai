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

public final class QuarkusConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  public QuarkusConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public QuarkusConfigurer noInjectionFields() {
    return noInjectionFields(defaultConfiguration());
  }

  public QuarkusConfigurer noInjectionFields(TaikaiRule.Configuration configuration) {
    return addRule(TaikaiRule.of(noFields()
        .should(be(annotatedWithInject(true)))
        .as("No fields should be annotated with %s, use constructor injection".formatted(
            QuarkusDescribedPredicates.ANNOTATION_INJECT)), configuration));
  }

  public QuarkusConfigurer resources(
      Customizer<ResourcesConfigurer> customizer) {
    return customizer(customizer, () -> new ResourcesConfigurer(configurerContext()));
  }

  public QuarkusConfigurer panache(
      Customizer<PanacheConfigurer> customizer) {
    return customizer(customizer, () -> new PanacheConfigurer(configurerContext()));
  }

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
