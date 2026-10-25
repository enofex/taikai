package com.enofex.taikai.jpa;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.internal.ArchConditions.haveFieldMatching;
import static com.enofex.taikai.internal.ArchConditions.notHaveFieldTypesMatching;
import static com.enofex.taikai.jpa.JpaDescribedPredicates.ANNOTATION_VERSION;
import static com.enofex.taikai.jpa.JpaDescribedPredicates.annotatedWithEntity;
import static com.enofex.taikai.jpa.JpaDescribedPredicates.annotatedWithEntityOrMappedSuperclass;
import static com.enofex.taikai.jpa.JpaDescribedPredicates.annotatedWithIdOrEmbeddedId;
import static com.enofex.taikai.jpa.JpaDescribedPredicates.annotatedWithVersion;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;

/**
 * Configures and enforces conventions for JPA entities using {@link com.tngtech.archunit ArchUnit}
 * through the Taikai framework.
 *
 * <p>This configurer ensures that entity classes follow consistent visibility, identifier,
 * optimistic locking, field type, and naming rules.</p>
 *
 * <h2>Example Usage</h2>
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
 *
 * <p>Entities are classes annotated with {@code jakarta.persistence.Entity}, including composed
 * annotations.</p>
 */
public final class EntitiesConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  EntitiesConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule enforcing that entities are package-private.
   *
   * @return this configurer for fluent chaining
   */
  public EntitiesConfigurer shouldBePackagePrivate() {
    return shouldBePackagePrivate(defaultConfiguration());
  }

  /**
   * See {@link #shouldBePackagePrivate()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public EntitiesConfigurer shouldBePackagePrivate(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithEntity(true)))
        .should().bePackagePrivate()
        .as("Entities should be package-private"), configuration));
  }

  /**
   * Prevents identifier fields, annotated with {@code jakarta.persistence.Id} or
   * {@code jakarta.persistence.EmbeddedId}, from having matching types, including generic
   * arguments, bounds, and array components. Identifier fields declared in entities and mapped
   * superclasses are checked, so an identifier inherited from a {@code @MappedSuperclass} is
   * checked as well.
   *
   * @param forbiddenTypes the forbidden identifier types, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public EntitiesConfigurer idsShouldNotBeOfType(
      DescribedPredicate<? super JavaClass> forbiddenTypes) {
    return idsShouldNotBeOfType(forbiddenTypes, defaultConfiguration());
  }

  /**
   * See {@link #idsShouldNotBeOfType(DescribedPredicate)}, with a custom configuration.
   *
   * @param forbiddenTypes the forbidden identifier types
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public EntitiesConfigurer idsShouldNotBeOfType(
      DescribedPredicate<? super JavaClass> forbiddenTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that(are(annotatedWithIdOrEmbeddedId(true)))
        .and().areDeclaredInClassesThat(are(annotatedWithEntityOrMappedSuperclass(true)))
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(notHaveFieldTypesMatching(forbiddenTypes))
        .as("Entity IDs should not be of types %s".formatted(forbiddenTypes.getDescription())),
        configuration));
  }

  /**
   * Adds a rule enforcing that entities have a field annotated with
   * {@code jakarta.persistence.Version} for optimistic locking. Inherited fields count, so a
   * version field declared in a {@code @MappedSuperclass} satisfies the rule.
   *
   * @return this configurer for fluent chaining
   */
  public EntitiesConfigurer shouldHaveFieldAnnotatedWithVersion() {
    return shouldHaveFieldAnnotatedWithVersion(defaultConfiguration());
  }

  /**
   * See {@link #shouldHaveFieldAnnotatedWithVersion()}, but with {@link Configuration} for
   * customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public EntitiesConfigurer shouldHaveFieldAnnotatedWithVersion(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithEntity(true)))
        .should(haveFieldMatching(annotatedWithVersion(true)))
        .as("Entities should have a field annotated with %s".formatted(ANNOTATION_VERSION)),
        configuration));
  }

  /**
   * Prevents fields declared in entities and mapped superclasses from having matching types,
   * including generic arguments, bounds, and array components. Synthetic fields generated by the
   * compiler are ignored.
   *
   * @param forbiddenTypes the forbidden field types, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public EntitiesConfigurer fieldsShouldNotBeOfType(
      DescribedPredicate<? super JavaClass> forbiddenTypes) {
    return fieldsShouldNotBeOfType(forbiddenTypes, defaultConfiguration());
  }

  /**
   * See {@link #fieldsShouldNotBeOfType(DescribedPredicate)}, with a custom configuration.
   *
   * @param forbiddenTypes the forbidden field types
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public EntitiesConfigurer fieldsShouldNotBeOfType(
      DescribedPredicate<? super JavaClass> forbiddenTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that().areDeclaredInClassesThat(are(annotatedWithEntityOrMappedSuperclass(true)))
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(notHaveFieldTypesMatching(forbiddenTypes))
        .as("Entity fields should not be of types %s".formatted(
            forbiddenTypes.getDescription())), configuration));
  }

  /**
   * Adds a rule enforcing that entities have names matching the given regex.
   *
   * @param regex the regex that fully qualified class names must match
   * @return this configurer for fluent chaining
   */
  public EntitiesConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex that fully qualified class names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public EntitiesConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithEntity(true)))
        .should().haveNameMatching(regex)
        .as("Entities should have names matching %s".formatted(regex)), configuration));
  }

  @Override
  public EntitiesConfigurer disable() {
    disable(EntitiesConfigurer.class);
    return this;
  }
}
