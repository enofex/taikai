package com.enfoex.taikai.java;

import com.enfoex.taikai.AbstractConfigurer;
import com.enfoex.taikai.ConfigurerContext;
import com.enfoex.taikai.Customizer;

public final class JavaConfigurer extends AbstractConfigurer {

  public JavaConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public JavaConfigurer imports(Customizer<ImportsConfigurer> customizer) {
    return customizer(customizer, () -> new ImportsConfigurer(configurerContext()));
  }

  @Override
  public void disable() {
    disable(ImportsConfigurer.class);
  }
}