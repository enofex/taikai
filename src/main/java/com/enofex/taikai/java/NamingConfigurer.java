package com.enofex.taikai.java;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.java.ConstantNaming.shouldFollowConstantNamingConventions;
import static com.enofex.taikai.java.PackageNaming.resideInPackageWithProperNamingConvention;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * Configures and enforces naming conventions for packages, classes, methods, fields, and constants
 * using {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that naming across a Java codebase remains consistent and predictable,
 * enforcing rules for package naming patterns, class prefixes/suffixes, method naming styles,
 * field names, and constant conventions. It allows customization for specific naming schemes
 * and integrates with the Taikai builder to apply these checks automatically.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .java(java -> java
 *         .naming(naming -> naming
 *             .packagesShouldMatchDefault()
 *             .classesShouldNotMatch(".*Impl$")
 *             .methodsShouldNotMatch("^temp.*")
 *             .fieldsShouldNotMatch("^debug.*")
 *             .interfacesShouldNotHavePrefixI()
 *             .constantsShouldFollowConventions()
 *         )
 *     );
 * }</pre>
 */
public class NamingConfigurer extends AbstractConfigurer {

  private static final String PACKAGE_NAME_REGEX = "^[a-z_]+(\\.[a-z_][a-z0-9_]*)*$";

  private static final Collection<String> DEFAULT_FIELDS_EXCLUDED_FROM_CONSTANT_NAMING = List.of(
      "serialVersionUID"
  );

  /**
   * Creates a new {@code NamingConfigurer} with the provided context.
   *
   * @param configurerContext the configuration context; must not be {@code null}
   */
  NamingConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Enforces the default Taikai package naming convention.
   *
   * @return this configurer for fluent chaining
   * @see #packagesShouldMatch(String)
   */
  public NamingConfigurer packagesShouldMatchDefault() {
    return packagesShouldMatch(PACKAGE_NAME_REGEX, defaultConfiguration());
  }

  /**
   * Enforces the default Taikai package naming convention using a custom configuration.
   *
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer packagesShouldMatchDefault(Configuration configuration) {
    return packagesShouldMatch(PACKAGE_NAME_REGEX, configuration);
  }

  /**
   * Enforces that packages match the given regular expression.
   *
   * @param regex the regular expression packages must match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer packagesShouldMatch(String regex) {
    return packagesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * Enforces that packages match the given regular expression using a custom configuration.
   *
   * @param regex the regular expression packages must match
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer packagesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(resideInPackageWithProperNamingConvention(regex))
        .as("Packages should have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Prohibits classes whose names match the given regular expression.
   *
   * @param regex a forbidden class-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesShouldNotMatch(String regex) {
    return classesShouldNotMatch(regex, defaultConfiguration());
  }

  /**
   * Prohibits classes whose names match the given regular expression using a custom configuration.
   *
   * @param regex a forbidden class-name pattern
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .should().haveNameMatching(regex)
        .as("Classes should not have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Requires classes annotated with the given annotation type to match the specified name pattern.
   *
   * @param annotationType the annotation type to filter classes
   * @param regex          the required class-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex) {
    return classesAnnotatedWithShouldMatch(annotationType.getName(), regex, defaultConfiguration());
  }

  /**
   * Requires classes annotated with the given annotation type to match the specified name pattern
   * using a custom configuration.
   *
   * @param annotationType the annotation type to filter classes
   * @param regex          the required class-name pattern
   * @param configuration  the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex, Configuration configuration) {
    return classesAnnotatedWithShouldMatch(annotationType.getName(), regex, configuration);
  }

  /**
   * Requires classes annotated with the given annotation (by fully qualified name)
   * to match the specified name pattern.
   *
   * @param annotationType the fully qualified annotation type name
   * @param regex          the required class-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesAnnotatedWithShouldMatch(String annotationType, String regex) {
    return classesAnnotatedWithShouldMatch(annotationType, regex, defaultConfiguration());
  }

  /**
   * Requires classes annotated with the given annotation (by fully qualified name)
   * to match the specified name pattern using a custom configuration.
   *
   * @param annotationType the fully qualified annotation type name
   * @param regex          the required class-name pattern
   * @param configuration  the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesAnnotatedWithShouldMatch(String annotationType, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().areMetaAnnotatedWith(annotationType)
        .should().haveNameMatching(regex)
        .as("Classes annotated with %s should have names matching %s".formatted(
            annotationType, regex)), configuration));
  }

  /**
   * Requires classes that implement the given type to match the specified name pattern.
   *
   * @param clazz the implemented type
   * @param regex the required class-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesImplementingShouldMatch(Class<?> clazz, String regex) {
    return classesImplementingShouldMatch(clazz.getName(), regex, defaultConfiguration());
  }

  /**
   * Requires classes that implement the given type to match the specified name pattern
   * using a custom configuration.
   *
   * @param clazz         the implemented type
   * @param regex         the required class-name pattern
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesImplementingShouldMatch(Class<?> clazz, String regex,
      Configuration configuration) {
    return classesImplementingShouldMatch(clazz.getName(), regex, configuration);
  }

  /**
   * Requires classes that implement the given type (by name) to match the specified name pattern.
   *
   * @param typeName the fully qualified implemented type name
   * @param regex    the required class-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesImplementingShouldMatch(String typeName, String regex) {
    return classesImplementingShouldMatch(typeName, regex, defaultConfiguration());
  }

  /**
   * Requires classes that implement the given type (by name) to match the specified name pattern
   * using a custom configuration.
   *
   * @param typeName      the fully qualified implemented type name
   * @param regex         the required class-name pattern
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesImplementingShouldMatch(String typeName, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().implement(typeName)
        .should().haveNameMatching(regex)
        .as("Classes implementing %s should have names matching %s".formatted(
            typeName, regex)), configuration));
  }

  /**
   * Requires classes assignable to the given type to match the specified name pattern.
   *
   * @param clazz the supertype or interface
   * @param regex the required class-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesAssignableToShouldMatch(Class<?> clazz, String regex) {
    return classesAssignableToShouldMatch(clazz.getName(), regex, defaultConfiguration());
  }

  /**
   * Requires classes assignable to the given type to match the specified name pattern
   * using a custom configuration.
   *
   * @param clazz         the supertype or interface
   * @param regex         the required class-name pattern
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesAssignableToShouldMatch(Class<?> clazz, String regex,
      Configuration configuration) {
    return classesAssignableToShouldMatch(clazz.getName(), regex, configuration);
  }

  /**
   * Requires classes assignable to the given type (by name) to match the specified name pattern.
   *
   * @param typeName the fully qualified supertype or interface name
   * @param regex    the required class-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesAssignableToShouldMatch(String typeName, String regex) {
    return classesAssignableToShouldMatch(typeName, regex, defaultConfiguration());
  }

  /**
   * Requires classes assignable to the given type (by name) to match the specified name pattern
   * using a custom configuration.
   *
   * @param typeName      the fully qualified supertype or interface name
   * @param regex         the required class-name pattern
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesAssignableToShouldMatch(String typeName, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().areAssignableTo(typeName)
        .should().haveNameMatching(regex)
        .as("Classes assignable to %s should have names matching %s".formatted(
            typeName, regex)), configuration));
  }

  /**
   * Requires methods annotated with the given annotation type to match the specified name pattern.
   *
   * @param annotationType the annotation type
   * @param regex          the required method-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer methodsAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex) {
    return methodsAnnotatedWithShouldMatch(annotationType.getName(), regex, defaultConfiguration());
  }

  /**
   * Requires methods annotated with the given annotation type to match the specified name pattern
   * using a custom configuration.
   *
   * @param annotationType the annotation type
   * @param regex          the required method-name pattern
   * @param configuration  the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer methodsAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex, Configuration configuration) {
    return methodsAnnotatedWithShouldMatch(annotationType.getName(), regex, configuration);
  }

  /**
   * Requires methods annotated with the given annotation (by name) to match the specified name pattern.
   *
   * @param annotationType the fully qualified annotation type name
   * @param regex          the required method-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer methodsAnnotatedWithShouldMatch(
      String annotationType, String regex) {
    return methodsAnnotatedWithShouldMatch(annotationType, regex, defaultConfiguration());
  }

  /**
   * Requires methods annotated with the given annotation (by name) to match the specified name pattern
   * using a custom configuration.
   *
   * @param annotationType the fully qualified annotation type name
   * @param regex          the required method-name pattern
   * @param configuration  the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer methodsAnnotatedWithShouldMatch(String annotationType, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().areMetaAnnotatedWith(annotationType)
        .should().haveNameMatching(regex)
        .as("Methods annotated with %s should have names matching %s".formatted(
            annotationType, regex)), configuration));
  }

  /**
   * Prohibits methods whose names match the given pattern.
   *
   * @param regex a forbidden method-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer methodsShouldNotMatch(String regex) {
    return methodsShouldNotMatch(regex, defaultConfiguration());
  }

  /**
   * Prohibits methods whose names match the given pattern using a custom configuration.
   *
   * @param regex         a forbidden method-name pattern
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer methodsShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noMethods()
        .should().haveNameMatching(regex)
        .as("Methods should not have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Requires fields annotated with the given annotation type to match the specified name pattern.
   *
   * @param annotationType the field-level annotation type
   * @param regex          the required field-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex) {
    return fieldsAnnotatedWithShouldMatch(annotationType.getName(), regex, defaultConfiguration());
  }

  /**
   * Requires fields annotated with the given annotation type to match the specified name pattern
   * using a custom configuration.
   *
   * @param annotationType the field-level annotation type
   * @param regex          the required field-name pattern
   * @param configuration  the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex, Configuration configuration) {
    return fieldsAnnotatedWithShouldMatch(annotationType.getName(), regex, configuration);
  }

  /**
   * Requires fields annotated with the given annotation (by name) to match the specified name pattern.
   *
   * @param annotationType the fully qualified annotation type name
   * @param regex          the required field-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsAnnotatedWithShouldMatch(String annotationType, String regex) {
    return fieldsAnnotatedWithShouldMatch(annotationType, regex, defaultConfiguration());
  }

  /**
   * Requires fields annotated with the given annotation (by name) to match the specified name pattern
   * using a custom configuration.
   *
   * @param annotationType the fully qualified annotation type name
   * @param regex          the required field-name pattern
   * @param configuration  the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsAnnotatedWithShouldMatch(String annotationType, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that().areMetaAnnotatedWith(annotationType)
        .should().haveNameMatching(regex)
        .as("Fields annotated with %s should have names matching %s".formatted(
            annotationType, regex)), configuration));
  }

  /**
   * Requires fields of the given raw type (by name) to match the specified name pattern.
   *
   * @param typeName the fully qualified field type name
   * @param regex    the required field-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsShouldMatch(String typeName, String regex) {
    return fieldsShouldMatch(typeName, regex, defaultConfiguration());
  }

  /**
   * Requires fields of the given raw type to match the specified name pattern.
   *
   * @param clazz the field raw type
   * @param regex the required field-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsShouldMatch(Class<?> clazz, String regex) {
    return fieldsShouldMatch(clazz.getName(), regex, defaultConfiguration());
  }

  /**
   * Requires fields of the given raw type to match the specified name pattern using a custom configuration.
   *
   * @param clazz         the field raw type
   * @param regex         the required field-name pattern
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsShouldMatch(Class<?> clazz, String regex,
      Configuration configuration) {
    return fieldsShouldMatch(clazz.getName(), regex, configuration);
  }

  /**
   * Requires fields of the given raw type (by name) to match the specified name pattern
   * using a custom configuration.
   *
   * @param typeName      the fully qualified field type name
   * @param regex         the required field-name pattern
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsShouldMatch(String typeName, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
            .that().haveRawType(typeName)
            .should().haveNameMatching(regex)
            .as("Fields of type %s should have names matching %s".formatted(typeName, regex)),
        configuration));
  }

  /**
   * Prohibits fields whose names match the given pattern.
   *
   * @param regex a forbidden field-name pattern
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsShouldNotMatch(String regex) {
    return fieldsShouldNotMatch(regex, defaultConfiguration());
  }

  /**
   * Prohibits fields whose names match the given pattern using a custom configuration.
   *
   * @param regex         a forbidden field-name pattern
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noFields()
        .should().haveNameMatching(regex)
        .as("Fields should not have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Enforces that interfaces are not prefixed with an upper-case {@code I} (e.g., {@code IFoo}).
   *
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer interfacesShouldNotHavePrefixI() {
    return interfacesShouldNotHavePrefixI(defaultConfiguration());
  }

  /**
   * Enforces that interfaces are not prefixed with an upper-case {@code I} using a custom configuration.
   *
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer interfacesShouldNotHavePrefixI(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().areInterfaces()
        .should(notBePrefixedWithI())
        .as("Interfaces should not be prefixed with I"), configuration));
  }

  /**
   * Creates an {@link ArchCondition} that fails if an interface name starts with {@code I}
   * followed by an uppercase character (e.g., {@code ICustomer}).
   *
   * @return the condition that detects {@code I}-prefixed interfaces
   */
  private static ArchCondition<JavaClass> notBePrefixedWithI() {
    return new ArchCondition<>("not be prefixed with I") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        if (javaClass.getSimpleName().startsWith("I") && Character.isUpperCase(
            javaClass.getSimpleName().charAt(1))) {
          events.add(SimpleConditionEvent.violated(javaClass, javaClass.getSimpleName()));
        }
      }
    };
  }

  /**
   * Enforces that constants (static final fields) follow conventional constant naming
   * (e.g., {@code UPPER_SNAKE_CASE}), excluding a default set of fields like {@code serialVersionUID}.
   *
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer constantsShouldFollowConventions() {
    return constantsShouldFollowConventions(DEFAULT_FIELDS_EXCLUDED_FROM_CONSTANT_NAMING,
        defaultConfiguration());
  }

  /**
   * Enforces constant naming conventions while excluding the specified field names.
   *
   * @param excludedFields field names to exclude from validation (e.g., {@code "serialVersionUID"})
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer constantsShouldFollowConventions(Collection<String> excludedFields) {
    return constantsShouldFollowConventions(excludedFields, defaultConfiguration());
  }

  /**
   * Enforces constant naming conventions using a custom configuration,
   * excluding a default field list.
   *
   * @param configuration the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer constantsShouldFollowConventions(Configuration configuration) {
    return constantsShouldFollowConventions(DEFAULT_FIELDS_EXCLUDED_FROM_CONSTANT_NAMING,
        configuration);
  }

  /**
   * Enforces constant naming conventions using a custom configuration and exclusion list.
   *
   * @param excludedFields field names to exclude from validation
   * @param configuration  the rule configuration to use
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer constantsShouldFollowConventions(Collection<String> excludedFields,
      Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that().areFinal().and().areStatic()
        .should(shouldFollowConstantNamingConventions(excludedFields))
        .as("Constants should follow constant naming conventions"), configuration));
  }
}
