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

public final class SpringConfigurer extends AbstractConfigurer {

  public SpringConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public SpringConfigurer properties(Customizer<PropertiesConfigurer> customizer) {
    return customizer(customizer, () -> new PropertiesConfigurer(configurerContext()));
  }

  public SpringConfigurer configurations(Customizer<ConfigurationsConfigurer> customizer) {
    return customizer(customizer, () -> new ConfigurationsConfigurer(configurerContext()));
  }

  public SpringConfigurer controllers(Customizer<ControllersConfigurer> customizer) {
    return customizer(customizer, () -> new ControllersConfigurer(configurerContext()));
  }

  public SpringConfigurer services(Customizer<ServicesConfigurer> customizer) {
    return customizer(customizer, () -> new ServicesConfigurer(configurerContext()));
  }

  public SpringConfigurer repositories(Customizer<RepositoriesConfigurer> customizer) {
    return customizer(customizer, () -> new RepositoriesConfigurer(configurerContext()));
  }

  public SpringConfigurer boot(Customizer<BootConfigurer> customizer) {
    return customizer(customizer, () -> new BootConfigurer(configurerContext()));
  }

  public SpringConfigurer noAutowiredFields() {
    return noAutowiredFields(defaultConfiguration());
  }

  public SpringConfigurer noAutowiredFields(Configuration configuration) {
    return addRule(TaikaiRule.of(noFields()
        .should(be(annotatedWithAutowired(true)))
        .as("No fields should be annotated with %s, use constructor injection".formatted(
            ANNOTATION_AUTOWIRED)), configuration));
  }

  @Override
  public void disable() {
    disable(SpringConfigurer.class);
    disable(PropertiesConfigurer.class);
    disable(ConfigurationsConfigurer.class);
    disable(ControllersConfigurer.class);
    disable(ServicesConfigurer.class);
    disable(RepositoriesConfigurer.class);
    disable(BootConfigurer.class);
  }
}