package com.enofex.taikai.test;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.configures.DisableableConfigurer;

public class TestConfigurer extends AbstractConfigurer {

  public TestConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * @deprecated Since only JUnit and above are supported, use {@link #junit(Customizer)} instead.
   * This method was retained for backward compatibility and delegates directly to
   * {@link #junit(Customizer)}.
   */
  @Deprecated(since = "1.3.9", forRemoval = true)
  public Disableable junit5(Customizer<JUnitConfigurer.Disableable> customizer) {
    return junit(customizer);
  }

  public Disableable junit(Customizer<JUnitConfigurer.Disableable> customizer) {
    return customizer(customizer, () -> new JUnitConfigurer.Disableable(configurerContext()));
  }

  public static final class Disableable extends TestConfigurer implements DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public TestConfigurer disable() {
      disable(TestConfigurer.class);
      disable(JUnitConfigurer.class);

      return this;
    }
  }
}