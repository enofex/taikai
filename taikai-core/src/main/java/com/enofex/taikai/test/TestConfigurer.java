package com.enofex.taikai.test;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.configures.DisableableConfigurer;

/**
 * Top-level configurer that groups test-related rules using {@link com.tngtech.archunit ArchUnit}
 * through the Taikai framework.
 *
 * <p>Currently, the only supported test framework is JUnit. Access JUnit-specific rules
 * via {@link #junit(Customizer)}.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
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