package com.enofex.taikai.jpa;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.configures.DisableableConfigurer;

/**
 * Top-level configurer for Jakarta Persistence (JPA) architectural rules using
 * {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>The rules only depend on the {@code jakarta.persistence} annotations, so they apply to any
 * JPA provider or framework, such as Spring Data JPA, Quarkus Hibernate ORM, or Jakarta EE.
 * Annotations are referenced by name, so no compile-time dependency on the JPA API is
 * required.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
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
