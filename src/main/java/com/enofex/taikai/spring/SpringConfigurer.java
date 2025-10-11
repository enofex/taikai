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

public class SpringConfigurer extends AbstractConfigurer {

  SpringConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public Disableable properties(Customizer<PropertiesConfigurer.Disableable> customizer) {
    return customizer(customizer, () -> new PropertiesConfigurer.Disableable(configurerContext()));
  }

  public Disableable configurations(
      Customizer<ConfigurationsConfigurer.Disableable> customizer) {
    return customizer(customizer, () -> new ConfigurationsConfigurer.Disableable(configurerContext()));
  }

  public Disableable controllers(
      Customizer<ControllersConfigurer.Disableable> customizer) {
    return customizer(customizer, () -> new ControllersConfigurer.Disableable(configurerContext()));
  }

  public Disableable services(Customizer<ServicesConfigurer.Disableable> customizer) {
    return customizer(customizer, () -> new ServicesConfigurer.Disableable(configurerContext()));
  }

  public Disableable repositories(
      Customizer<RepositoriesConfigurer.Disableable> customizer) {
    return customizer(customizer, () -> new RepositoriesConfigurer.Disableable(configurerContext()));
  }

  public Disableable boot(Customizer<BootConfigurer.Disableable> customizer) {
    return customizer(customizer, () -> new BootConfigurer.Disableable(configurerContext()));
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

  public static final class Disableable extends SpringConfigurer implements DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
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

      return this;
    }
  }

}