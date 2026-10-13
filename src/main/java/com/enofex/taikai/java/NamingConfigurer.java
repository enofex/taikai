package com.enofex.taikai.java;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.java.BooleanMethodNaming.haveBooleanReturnType;
import static com.enofex.taikai.java.BooleanMethodNaming.shouldStartWith;
import static com.enofex.taikai.java.ConstantNaming.shouldFollowConstantNamingConventions;
import static com.enofex.taikai.java.EnumConstantNaming.shouldFollowEnumConstantNamingConventions;
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
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.List;

/**
 * Configures and enforces naming conventions for packages, classes, methods, fields, and constants
 * using {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that naming across a Java codebase remains consistent and
 * predictable,
 * enforcing rules for package naming patterns, class prefixes/suffixes, method naming styles, field
 * names, and constant conventions. It allows customization for specific naming schemes and
 * integrates with the Taikai builder to apply these checks automatically.</p>
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
 *             .enumConstantsShouldFollowConventions()
 *             .booleanMethodsShouldStartWith()
 *         )
 *     );
 * }</pre>
 */
public final class NamingConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  private static final String PACKAGE_NAME_REGEX = "^[a-z_]+(\\.[a-z_][a-z0-9_]*)*$";

  private static final Collection<String> DEFAULT_FIELDS_EXCLUDED_FROM_CONSTANT_NAMING = List.of(
      "serialVersionUID"
  );

  private static final Collection<String> DEFAULT_BOOLEAN_METHOD_PREFIXES = List.of(
      "is", "has", "can", "should"
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
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer packagesShouldMatchDefault(Configuration configuration) {
    return packagesShouldMatch(PACKAGE_NAME_REGEX, configuration);
  }

  /**
   * Enforces that packages match the given regular expression.
   *
   * @param regex the regex that package names must match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer packagesShouldMatch(String regex) {
    return packagesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * Enforces that packages match the given regular expression using a custom configuration.
   *
   * @param regex the regex that package names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer packagesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(resideInPackageWithProperNamingConvention(regex))
        .as("Packages should have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Requires all classes to have names matching the given regular expression.
   *
   * @param regex the regex that fully qualified class names must match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesShouldMatch(String regex) {
    return classesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * Requires all classes to have names matching the given regular expression using a custom
   * configuration.
   *
   * @param regex the regex that fully qualified class names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should().haveNameMatching(regex)
        .as("Classes should have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Prohibits classes whose names match the given regular expression.
   *
   * @param regex the regex that fully qualified class names must not match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesShouldNotMatch(String regex) {
    return classesShouldNotMatch(regex, defaultConfiguration());
  }

  /**
   * Prohibits classes whose names match the given regular expression using a custom configuration.
   *
   * @param regex the regex that fully qualified class names must not match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .should().haveNameMatching(regex)
        .as("Classes should not have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Requires classes matching the given criteria to match the specified name pattern.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria} or an
   *     ArchUnit predicate
   * @param regex the regex that fully qualified class names must match
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer classesShouldMatch(DescribedPredicate<? super JavaClass> classes,
      String regex) {
    return classesShouldMatch(classes, regex, defaultConfiguration());
  }

  /**
   * Requires classes matching the given criteria to match the specified name pattern using a
   * custom configuration.
   *
   * @param classes the classes to check
   * @param regex the regex that fully qualified class names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer classesShouldMatch(DescribedPredicate<? super JavaClass> classes,
      String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(classes)
        .should().haveNameMatching(regex)
        .as("Classes %s should have names matching %s".formatted(
            classes.getDescription(), regex)), configuration));
  }

  /**
   * Prohibits classes matching the given criteria from having names that match the specified
   * pattern.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria} or an
   *     ArchUnit predicate
   * @param regex the regex that fully qualified class names must not match
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer classesShouldNotMatch(DescribedPredicate<? super JavaClass> classes,
      String regex) {
    return classesShouldNotMatch(classes, regex, defaultConfiguration());
  }

  /**
   * Prohibits classes matching the given criteria from having names that match the specified
   * pattern using a custom configuration.
   *
   * @param classes the classes to check
   * @param regex the regex that fully qualified class names must not match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer classesShouldNotMatch(DescribedPredicate<? super JavaClass> classes,
      String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .that(classes)
        .should().haveNameMatching(regex)
        .as("Classes %s should not have names matching %s".formatted(
            classes.getDescription(), regex)), configuration));
  }

  /**
   * Requires all methods to have names matching the given pattern.
   *
   * @param regex the regex that method names must match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer methodsShouldMatch(String regex) {
    return methodsShouldMatch(regex, defaultConfiguration());
  }

  /**
   * Requires all methods to have names matching the given pattern using a custom configuration.
   *
   * @param regex the regex that method names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer methodsShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().haveNameMatching(regex)
        .as("Methods should have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Requires methods matching the given criteria to match the specified name pattern. Synthetic
   * methods generated by the compiler are ignored.
   *
   * @param methods the methods to check, selected with {@link com.enofex.taikai.MethodCriteria}, for
   *     example {@code MethodCriteria.annotatedWith(Test.class)}, or an ArchUnit predicate
   * @param regex the regex that method names must match
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer methodsShouldMatch(DescribedPredicate<? super JavaMethod> methods,
      String regex) {
    return methodsShouldMatch(methods, regex, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldMatch(DescribedPredicate, String)}, with a custom configuration.
   *
   * @param methods the methods to check
   * @param regex the regex that method names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer methodsShouldMatch(DescribedPredicate<? super JavaMethod> methods,
      String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(methods)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().haveNameMatching(regex)
            .as("Methods %s should have names matching %s".formatted(
                methods.getDescription(), regex)),
        configuration));
  }

  /**
   * Prohibits methods whose names match the given pattern.
   *
   * @param regex the regex that method names must not match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer methodsShouldNotMatch(String regex) {
    return methodsShouldNotMatch(regex, defaultConfiguration());
  }

  /**
   * Prohibits methods whose names match the given pattern using a custom configuration.
   *
   * @param regex the regex that method names must not match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer methodsShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noMethods()
        .that().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().haveNameMatching(regex)
        .as("Methods should not have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Requires methods matching the given criteria not to match the specified name pattern.
   * Synthetic methods generated by the compiler are ignored.
   *
   * @param methods the methods to check, selected with {@link com.enofex.taikai.MethodCriteria}, for
   *     example {@code MethodCriteria.annotatedWith(Deprecated.class)}, or an ArchUnit predicate
   * @param regex the regex that method names must not match
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer methodsShouldNotMatch(DescribedPredicate<? super JavaMethod> methods,
      String regex) {
    return methodsShouldNotMatch(methods, regex, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotMatch(DescribedPredicate, String)}, with a custom configuration.
   *
   * @param methods the methods to check
   * @param regex the regex that method names must not match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer methodsShouldNotMatch(DescribedPredicate<? super JavaMethod> methods,
      String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noMethods()
            .that(methods)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().haveNameMatching(regex)
            .as("Methods %s should not have names matching %s".formatted(
                methods.getDescription(), regex)),
        configuration));
  }

  /**
   * Requires all methods declared in classes matching the given criteria to have names matching
   * the given pattern. Synthetic methods generated by the compiler are ignored.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param regex the regex that method names must match
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer methodsShouldMatchForClass(
      DescribedPredicate<? super JavaClass> classes, String regex) {
    return methodsShouldMatchForClass(classes, regex, defaultConfiguration());
  }

  /**
   * Requires all methods declared in classes matching the given criteria to have names matching
   * the given pattern using a custom configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param regex the regex that method names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer methodsShouldMatchForClass(
      DescribedPredicate<? super JavaClass> classes, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().areDeclaredInClassesThat(classes)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().haveNameMatching(regex)
        .as("Methods declared in classes %s should have names matching %s".formatted(
            classes.getDescription(), regex)), configuration));
  }

  /**
   * Prohibits methods declared in classes matching the given criteria from having names that
   * match the given pattern. Synthetic methods generated by the compiler are ignored.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param regex the regex that method names must not match
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer methodsShouldNotMatchForClass(
      DescribedPredicate<? super JavaClass> classes, String regex) {
    return methodsShouldNotMatchForClass(classes, regex, defaultConfiguration());
  }

  /**
   * Prohibits methods declared in classes matching the given criteria from having names that
   * match the given pattern using a custom configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param regex the regex that method names must not match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer methodsShouldNotMatchForClass(
      DescribedPredicate<? super JavaClass> classes, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(noMethods()
        .that().areDeclaredInClassesThat(classes)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().haveNameMatching(regex)
        .as("Methods declared in classes %s should not have names matching %s".formatted(
            classes.getDescription(), regex)), configuration));
  }

  /**
   * Requires fields matching the given criteria to match the specified name pattern. Synthetic
   * fields generated by the compiler are ignored.
   *
   * @param fields the fields to check, selected with {@link com.enofex.taikai.FieldCriteria}, for
   *     example {@code FieldCriteria.annotatedWith(Inject.class)}, or an ArchUnit predicate
   * @param regex the regex that field names must match
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.FieldCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer fieldsShouldMatch(DescribedPredicate<? super JavaField> fields,
      String regex) {
    return fieldsShouldMatch(fields, regex, defaultConfiguration());
  }

  /**
   * See {@link #fieldsShouldMatch(DescribedPredicate, String)}, with a custom configuration.
   *
   * @param fields the fields to check
   * @param regex the regex that field names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.FieldCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer fieldsShouldMatch(DescribedPredicate<? super JavaField> fields,
      String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
            .that(fields)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().haveNameMatching(regex)
            .as("Fields %s should have names matching %s".formatted(
                fields.getDescription(), regex)),
        configuration));
  }

  /**
   * Prohibits fields whose names match the given pattern.
   *
   * @param regex the regex that field names must not match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsShouldNotMatch(String regex) {
    return fieldsShouldNotMatch(regex, defaultConfiguration());
  }

  /**
   * Prohibits fields whose names match the given pattern using a custom configuration.
   *
   * @param regex the regex that field names must not match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noFields()
        .that().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().haveNameMatching(regex)
        .as("Fields should not have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Requires fields matching the given criteria not to match the specified name pattern.
   * Synthetic fields generated by the compiler are ignored.
   *
   * @param fields the fields to check, selected with {@link com.enofex.taikai.FieldCriteria}, for
   *     example {@code FieldCriteria.ofType(String.class)}, or an ArchUnit predicate
   * @param regex the regex that field names must not match
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.FieldCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer fieldsShouldNotMatch(DescribedPredicate<? super JavaField> fields,
      String regex) {
    return fieldsShouldNotMatch(fields, regex, defaultConfiguration());
  }

  /**
   * See {@link #fieldsShouldNotMatch(DescribedPredicate, String)}, with a custom configuration.
   *
   * @param fields the fields to check
   * @param regex the regex that field names must not match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.FieldCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer fieldsShouldNotMatch(DescribedPredicate<? super JavaField> fields,
      String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noFields()
            .that(fields)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().haveNameMatching(regex)
            .as("Fields %s should not have names matching %s".formatted(
                fields.getDescription(), regex)),
        configuration));
  }

  /**
   * Requires all fields declared in classes matching the given criteria to have names matching
   * the given pattern. Synthetic fields generated by the compiler are ignored.
   *
   * @param classes the classes whose declared fields are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param regex the regex that field names must match
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer fieldsShouldMatchForClass(
      DescribedPredicate<? super JavaClass> classes, String regex) {
    return fieldsShouldMatchForClass(classes, regex, defaultConfiguration());
  }

  /**
   * Requires all fields declared in classes matching the given criteria to have names matching
   * the given pattern using a custom configuration.
   *
   * @param classes the classes whose declared fields are checked
   * @param regex the regex that field names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer fieldsShouldMatchForClass(
      DescribedPredicate<? super JavaClass> classes, String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that().areDeclaredInClassesThat(classes)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().haveNameMatching(regex)
        .as("Fields declared in classes %s should have names matching %s".formatted(
            classes.getDescription(), regex)), configuration));
  }

  /**
   * Prohibits fields declared in classes matching the given criteria from having names that match
   * the given pattern. Synthetic fields generated by the compiler are ignored.
   *
   * @param classes the classes whose declared fields are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param regex the regex that field names must not match
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer fieldsShouldNotMatchForClass(
      DescribedPredicate<? super JavaClass> classes, String regex) {
    return fieldsShouldNotMatchForClass(classes, regex, defaultConfiguration());
  }

  /**
   * Prohibits fields declared in classes matching the given criteria from having names that match
   * the given pattern using a custom configuration.
   *
   * @param classes the classes whose declared fields are checked
   * @param regex the regex that field names must not match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public NamingConfigurer fieldsShouldNotMatchForClass(
      DescribedPredicate<? super JavaClass> classes, String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noFields()
        .that().areDeclaredInClassesThat(classes)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().haveNameMatching(regex)
        .as("Fields declared in classes %s should not have names matching %s".formatted(
            classes.getDescription(), regex)), configuration));
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
   * Enforces that interfaces are not prefixed with an upper-case {@code I} using a custom
   * configuration.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer interfacesShouldNotHavePrefixI(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().areInterfaces()
        .should(notBePrefixedWithI())
        .as("Interfaces should not be prefixed with I"), configuration));
  }

  /**
   * Creates an {@link ArchCondition} that fails if an interface name starts with {@code I} followed
   * by an uppercase character (e.g., {@code ICustomer}).
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
   * Enforces that constants (static final fields) follow conventional constant naming (e.g.,
   * {@code UPPER_SNAKE_CASE}), excluding a default set of fields like {@code serialVersionUID}.
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
   * @param excludedFields field names to exclude from validation (e.g.,
   *     {@code "serialVersionUID"})
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer constantsShouldFollowConventions(Collection<String> excludedFields) {
    return constantsShouldFollowConventions(excludedFields, defaultConfiguration());
  }

  /**
   * Enforces constant naming conventions using a custom configuration, excluding a default field
   * list.
   *
   * @param configuration the rule configuration
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
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer constantsShouldFollowConventions(Collection<String> excludedFields,
      Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that().areFinal().and().areStatic()
        .should(shouldFollowConstantNamingConventions(excludedFields))
        .as("Constants should follow constant naming conventions"), configuration));
  }

  /**
   * Enforces that enum constants follow conventional constant naming (e.g.,
   * {@code UPPER_SNAKE_CASE}).
   *
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer enumConstantsShouldFollowConventions() {
    return enumConstantsShouldFollowConventions(defaultConfiguration());
  }

  /**
   * Enforces enum constant naming conventions using a custom configuration.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer enumConstantsShouldFollowConventions(Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that().areDeclaredInClassesThat().areEnums()
        .should(shouldFollowEnumConstantNamingConventions())
        .as("Enum constants should follow constant naming conventions"), configuration));
  }

  /**
   * Enforces that methods returning {@code boolean} or {@code Boolean} start with one of the
   * default prefixes {@code is}, {@code has}, {@code can}, or {@code should}.
   *
   * <p>Synthetic methods, {@code equals(Object)} overrides, and record component accessors are
   * excluded from this check.</p>
   *
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer booleanMethodsShouldStartWith() {
    return booleanMethodsShouldStartWith(DEFAULT_BOOLEAN_METHOD_PREFIXES, defaultConfiguration());
  }

  /**
   * Enforces boolean method naming with the given prefixes.
   *
   * @param prefixes the allowed method name prefixes (e.g., {@code "is"}, {@code "has"})
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer booleanMethodsShouldStartWith(Collection<String> prefixes) {
    return booleanMethodsShouldStartWith(prefixes, defaultConfiguration());
  }

  /**
   * Enforces boolean method naming with the default prefixes using a custom configuration.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer booleanMethodsShouldStartWith(Configuration configuration) {
    return booleanMethodsShouldStartWith(DEFAULT_BOOLEAN_METHOD_PREFIXES, configuration);
  }

  /**
   * Enforces boolean method naming with the given prefixes using a custom configuration.
   *
   * @param prefixes the allowed method name prefixes (e.g., {@code "is"}, {@code "has"})
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer booleanMethodsShouldStartWith(Collection<String> prefixes,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that(haveBooleanReturnType())
        .should(shouldStartWith(prefixes))
        .as("Methods returning boolean or Boolean should start with one of %s".formatted(prefixes)),
        configuration));
  }

  @Override
  public NamingConfigurer disable() {
    disable(NamingConfigurer.class);

    return this;
  }
}
