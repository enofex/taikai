package com.enfoex.taikai.spring;

import com.enfoex.taikai.AbstractConfigurer;
import com.enfoex.taikai.Customizer;
import com.enfoex.taikai.Configurers;
import java.util.Objects;

public final class SpringConfigurer extends AbstractConfigurer {

  public SpringConfigurer(Configurers configurers) {
    super(configurers);
  }

  public SpringConfigurer controllers(Customizer<ControllersConfigurer> customizer) {
    Objects.requireNonNull(customizer);
    customizer.customize(configurers().getOrApply(new ControllersConfigurer(configurers())));
    return this;
  }

  public SpringConfigurer configurations(Customizer<ConfigurationsConfigurer> customizer) {
    Objects.requireNonNull(customizer);
    customizer.customize(configurers().getOrApply(new ConfigurationsConfigurer(configurers())));
    return this;
  }

  @Override
  public void disable() {
    disable(ControllersConfigurer.class);
    disable(ConfigurationsConfigurer.class);
  }
}
