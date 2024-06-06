package com.enofex.taikai.test;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;

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