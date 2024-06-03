package com.enfoex.taikai.test;

import com.enfoex.taikai.AbstractConfigurer;
import com.enfoex.taikai.ConfigurerContext;
import com.enfoex.taikai.Customizer;

public final class TestConfigurer extends AbstractConfigurer {

  public TestConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public TestConfigurer junit5(Customizer<JUnit5Configurer> customizer) {
    return customizer(customizer, () -> new JUnit5Configurer(configurerContext()));
  }

  @Override
  public void disable() {
    disable(JUnit5Configurer.class);
  }
}