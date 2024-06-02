package com.enfoex.taikai.spring;

import com.enfoex.taikai.AbstractConfigurer;
import com.enfoex.taikai.ConfigurerContext;
import com.enfoex.taikai.Customizer;
import java.util.Objects;

public final class SpringConfigurer extends AbstractConfigurer {

  public SpringConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public SpringConfigurer controllers(Customizer<ControllersConfigurer> customizer) {
    Objects.requireNonNull(customizer);
    customizer.customize(configurerContext()
        .configurers()
        .getOrApply(new ControllersConfigurer(configurerContext())));
    return this;
  }

  public SpringConfigurer configurations(Customizer<ConfigurationsConfigurer> customizer) {
    Objects.requireNonNull(customizer);
    customizer.customize(configurerContext()
        .configurers()
        .getOrApply(new ConfigurationsConfigurer(configurerContext())));
    return this;
  }

  @Override
  public void disable() {
    disable(ControllersConfigurer.class);
    disable(ConfigurationsConfigurer.class);
  }
}
