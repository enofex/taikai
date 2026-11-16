package com.enofex.taikai.jpa;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.configures.DisableableConfigurer;

/**
 * Rules for Jakarta Persistence (JPA) code, which apply to any JPA provider, as they only reference
 * the {@code jakarta.persistence} annotations by name:
 *
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .jpa(jpa -> jpa
 *         .entities(entities -> entities
 *             .shouldBePackagePrivate()
 *             .idsShouldNotBeOfType(ClassCriteria.type(UUID.class))
 *             .shouldHaveFieldAnnotatedWithVersion()
 *             .fieldsShouldNotBeOfType(
 *                 ClassCriteria.type(Date.class).or(ClassCriteria.assignableTo(Date.class)))
 *             .namesShouldMatch(".+Entity"))
 *     )
 *     .build()
 *     .check();
 * }</pre>
 */
public final class JpaConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  /**
   * @param configurerContext the context of the builder
   */
  public JpaConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Configures {@code @Entity} class rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link EntitiesConfigurer}
   * @return this configurer for fluent chaining
   */
  public JpaConfigurer entities(Customizer<EntitiesConfigurer> customizer) {
    return customizer(customizer, () -> new EntitiesConfigurer(configurerContext()));
  }

  @Override
  public JpaConfigurer disable() {
    disable(JpaConfigurer.class);
    disable(EntitiesConfigurer.class);

    return this;
  }
}
