package com.enfoex.taikai.spring;

import com.enfoex.taikai.AbstractConfigurer;
import com.enfoex.taikai.ConfigurerContext;
import com.enfoex.taikai.Customizer;

public final class SpringConfigurer extends AbstractConfigurer {

  public SpringConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
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

  @Override
  public void disable() {
    disable(ConfigurationsConfigurer.class);
    disable(ControllersConfigurer.class);
    disable(ServicesConfigurer.class);
    disable(RepositoriesConfigurer.class);
  }
}