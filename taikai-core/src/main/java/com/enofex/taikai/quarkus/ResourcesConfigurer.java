package com.enofex.taikai.quarkus;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.quarkus.QuarkusDescribedPredicates.ANNOTATION_PATH;
import static com.enofex.taikai.quarkus.QuarkusDescribedPredicates.annotatedWithPath;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.conditions.ArchConditions.onlyHaveDependentClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Configures and enforces conventions for Quarkus (and Jakarta EE) Rest APIs
 * using {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that resources classes follow consistent naming,
 * annotations, and dependency rules, promoting clarity and maintainability.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .quarkus(quarkus -> quarkus
 *         .resources(ctrl -> ctrl
 *             .namesShouldEndWithResource()
 *             .shouldBeAnnotatedWithPath()
 *             .shouldBePublic()
 *             .shouldNotDependOnOtherResources()
 *         )
 *     );
 * }</pre>
 *
 * <p>By default, this configurer checks classes annotated with {@code @Path}.</p>
 */
public final class ResourcesConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  private static final String DEFAULT_RESOURCE_NAME_MATCHING = ".+Resource";

  ResourcesConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Adds a rule enforcing that resource have names ending with {@code Resource}.
   *
   * @return this configurer for fluent chaining
   */
  public ResourcesConfigurer namesShouldEndWithResource() {
    return namesShouldMatch(DEFAULT_RESOURCE_NAME_MATCHING, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldEndWithResource()}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ResourcesConfigurer namesShouldEndWithResource(TaikaiRule.Configuration configuration) {
    return namesShouldMatch(DEFAULT_RESOURCE_NAME_MATCHING, configuration);
  }

  /**
   * Adds a rule enforcing that resource class names match the given regex.
   *
   * @param regex the regex that fully qualified class names must match
   * @return this configurer for fluent chaining
   */
  public ResourcesConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #namesShouldMatch(String)}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param regex the regex that fully qualified class names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ResourcesConfigurer namesShouldMatch(String regex, TaikaiRule.Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithPath(true)))
        .should().haveNameMatching(regex)
        .as("Resources should have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Adds a rule enforcing that resource classes should have public visibility.
   *
   * @return this configurer for fluent chaining
   */
  public ResourcesConfigurer shouldBePublic() {
    return shouldBePublic(defaultConfiguration());
  }

  /**
   * See {@link #shouldBePublic()}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ResourcesConfigurer shouldBePublic(TaikaiRule.Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithPath(true)))
        .should().bePublic()
        .as("Resources should be public"), configuration));
  }

  /**
   * Adds a rule that resource classes (matching the default name pattern)
   * should be annotated with {@code @Path}. Interfaces are excluded.
   *
   * @return this configurer for fluent chaining
   */
  public ResourcesConfigurer shouldBeAnnotatedWithPath() {
    return shouldBeAnnotatedWithPath(
        ClassCriteria.nameMatching(DEFAULT_RESOURCE_NAME_MATCHING), defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithPath()}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ResourcesConfigurer shouldBeAnnotatedWithPath(TaikaiRule.Configuration configuration) {
    return shouldBeAnnotatedWithPath(
        ClassCriteria.nameMatching(DEFAULT_RESOURCE_NAME_MATCHING), configuration);
  }

  /**
   * Adds a rule that resource classes matching the given criteria
   * should be annotated with {@code @Path}. Interfaces are excluded.
   *
   * @param classes the resource classes, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ResourcesConfigurer shouldBeAnnotatedWithPath(DescribedPredicate<? super JavaClass> classes) {
    return shouldBeAnnotatedWithPath(classes, defaultConfiguration());
  }

  /**
   * See {@link #shouldBeAnnotatedWithPath(DescribedPredicate)}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param classes the resource classes
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public ResourcesConfigurer shouldBeAnnotatedWithPath(DescribedPredicate<? super JavaClass> classes, TaikaiRule.Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areNotInterfaces().and(classes)
            .should(be(annotatedWithPath(true)))
            .as("Resources %s should be annotated with %s".formatted(
                classes.getDescription(), ANNOTATION_PATH)),
        configuration));
  }

  /**
   * Adds a rule enforcing that resource classes should not depend on other resources.
   *
   * @return this configurer for fluent chaining
   */
  public ResourcesConfigurer shouldNotDependOnOtherResources() {
    return shouldNotDependOnOtherResources(defaultConfiguration());
  }

  /**
   * See {@link #shouldNotDependOnOtherResources()}, but with {@link TaikaiRule.Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public ResourcesConfigurer shouldNotDependOnOtherResources(TaikaiRule.Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithPath(true)))
        .should(not(onlyHaveDependentClassesThat(are(annotatedWithPath(true)))))
        .as("Resources should not depend on other Resources"), configuration));
  }

  @Override
  public ResourcesConfigurer disable() {
    disable(ResourcesConfigurer.class);

    return this;
  }

}
