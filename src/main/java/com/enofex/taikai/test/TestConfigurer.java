package com.enofex.taikai.test;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.configures.DisableableConfigurer;

public class TestConfigurer extends AbstractConfigurer {

  public TestConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public Disableable junit5(Customizer<JUnit5Configurer.Disableable> customizer) {
    return customizer(customizer, () -> new JUnit5Configurer.Disableable(configurerContext()));
  }

  public static final class Disableable extends TestConfigurer implements DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public TestConfigurer disable() {
      disable(TestConfigurer.class);
      disable(JUnit5Configurer.class);

      return this;
    }
  }
}