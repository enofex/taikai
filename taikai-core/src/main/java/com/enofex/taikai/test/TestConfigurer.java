package com.enofex.taikai.test;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.configures.DisableableConfigurer;

/**
 * Rules for tests, currently for JUnit with {@link #junit(Customizer)}:
 *
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .test(test -> test
 *         .junit(junit -> junit
 *             .methodsShouldMatch("should[A-Z].*")
 *             .methodsShouldBePackagePrivate()
 *             .methodsShouldContainAssertionsOrVerifications()
 *             .classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Test"))
 *             .classesShouldNotBeAnnotatedWithDisabled()
 *         )
 *     )
 *     .build()
 *     .check();
 * }</pre>
 */
public final class TestConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  /**
   * @param configurerContext the context of the builder
   */
  public TestConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Configures JUnit-specific test rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link JUnitConfigurer}
   * @return this configurer for fluent chaining
   */
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
