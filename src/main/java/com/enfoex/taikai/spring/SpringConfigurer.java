package com.enfoex.taikai.spring;

import com.enfoex.taikai.AbstractConfigurer;
import com.enfoex.taikai.ConfigurerContext;
import com.enfoex.taikai.Customizer;

public final class SpringConfigurer extends AbstractConfigurer {

  public SpringConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public SpringConfigurer configurations(Customizer<ConfigurationsConfigurer> customizer) {
    customizer(customizer, () -> new ConfigurationsConfigurer(configurerContext()));
    return this;
  }

  public SpringConfigurer controllers(Customizer<ControllersConfigurer> customizer) {
    customizer(customizer, () -> new ControllersConfigurer(configurerContext()));
    return this;
  }

  public SpringConfigurer services(Customizer<ServicesConfigurer> customizer) {
    customizer(customizer, () -> new ServicesConfigurer(configurerContext()));
    return this;
  }

  public SpringConfigurer repositories(Customizer<RepositoriesConfigurer> customizer) {
    customizer(customizer, () -> new RepositoriesConfigurer(configurerContext()));
    return this;
  }

  @Override
  public void disable() {
    disable(ConfigurationsConfigurer.class);
    disable(ControllersConfigurer.class);
    disable(ServicesConfigurer.class);
    disable(RepositoriesConfigurer.class);
  }
}