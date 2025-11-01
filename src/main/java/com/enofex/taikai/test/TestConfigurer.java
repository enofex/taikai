package com.enofex.taikai.test;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.configures.DisableableConfigurer;

public final class TestConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  public TestConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * @deprecated Since only JUnit and above are supported, use {@link #junit(Customizer)} instead.
   * This method was retained for backward compatibility and delegates directly to
   * {@link #junit(Customizer)}.
   */
  @Deprecated(since = "1.3.9", forRemoval = true)
  public TestConfigurer junit5(Customizer<JUnitConfigurer> customizer) {
    return junit(customizer);
  }

  public TestConfigurer junit(Customizer<JUnitConfigurer> customizer) {
    return customizer(customizer, () -> new JUnitConfigurer(configurerContext()));
  }

  @Override
  public TestConfigurer disable() {
    disable(TestConfigurer.class);
    disable(JUnitConfigurer.class);

    return this;
  }
}