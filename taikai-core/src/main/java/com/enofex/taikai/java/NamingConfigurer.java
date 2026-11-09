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
 * Configures and enforces naming conventions for packages, classes, methods, fields and constants
 * using {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer ensures that naming across a Java codebase remains consistent and
 * predictable, enforcing rules for package naming patterns, class prefixes/suffixes, method naming
 * styles, field names and constant conventions. It allows customization for specific naming
 * schemes and integrates with the Taikai builder to apply these checks automatically.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
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
   * Adds a rule that packages must follow the default Taikai package naming convention.
   *
   * @return this configurer for fluent chaining
   * @see #packagesShouldMatch(String)
   */
  public NamingConfigurer packagesShouldMatchDefault() {
    return packagesShouldMatch(PACKAGE_NAME_REGEX, defaultConfiguration());
  }

  /**
   * See {@link #packagesShouldMatchDefault()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer packagesShouldMatchDefault(Configuration configuration) {
    return packagesShouldMatch(PACKAGE_NAME_REGEX, configuration);
  }

  /**
   * Adds a rule that packages must match the given regex.
   *
   * @param regex the regex that package names must match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer packagesShouldMatch(String regex) {
    return packagesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #packagesShouldMatch(String)}, but with {@link Configuration} for customization.
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
   * Adds a rule that all classes must have names matching the given regex.
   *
   * @param regex the regex that fully qualified class names must match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesShouldMatch(String regex) {
    return classesShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex that fully qualified class names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().haveNameMatching(regex)
        .as("Classes should have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Adds a rule that classes must not have names matching the given regex.
   *
   * @param regex the regex that fully qualified class names must not match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesShouldNotMatch(String regex) {
    return classesShouldNotMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldNotMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex that fully qualified class names must not match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer classesShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .that().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().haveNameMatching(regex)
        .as("Classes should not have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Adds a rule that classes matching the given criteria must have names matching the given regex.
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
   * See {@link #classesShouldMatch(DescribedPredicate, String)}, but with {@link Configuration} for
   * customization.
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
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().haveNameMatching(regex)
        .as("Classes %s should have names matching %s".formatted(
            classes.getDescription(), regex)), configuration));
  }

  /**
   * Adds a rule that classes matching the given criteria must not have names matching the given
   * regex.
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
   * See {@link #classesShouldNotMatch(DescribedPredicate, String)}, but with {@link Configuration}
   * for customization.
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
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().haveNameMatching(regex)
        .as("Classes %s should not have names matching %s".formatted(
            classes.getDescription(), regex)), configuration));
  }

  /**
   * Adds a rule that all methods must have names matching the given regex.
   *
   * @param regex the regex that method names must match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer methodsShouldMatch(String regex) {
    return methodsShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldMatch(String)}, but with {@link Configuration} for customization.
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
   * Adds a rule that methods matching the given criteria must have names matching the given regex.
   * Synthetic methods generated by the compiler are ignored.
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
   * See {@link #methodsShouldMatch(DescribedPredicate, String)}, but with {@link Configuration} for
   * customization.
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
   * Adds a rule that methods must not have names matching the given regex.
   *
   * @param regex the regex that method names must not match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer methodsShouldNotMatch(String regex) {
    return methodsShouldNotMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotMatch(String)}, but with {@link Configuration} for customization.
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
   * Adds a rule that methods matching the given criteria must not have names matching the given
   * regex. Synthetic methods generated by the compiler are ignored.
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
   * See {@link #methodsShouldNotMatch(DescribedPredicate, String)}, but with {@link Configuration}
   * for customization.
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
   * Adds a rule that all methods declared in classes matching the given criteria must have names
   * matching the given regex. Synthetic methods generated by the compiler are ignored.
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
   * See {@link #methodsShouldMatchForClass(DescribedPredicate, String)}, but with
   * {@link Configuration} for customization.
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
   * Adds a rule that methods declared in classes matching the given criteria must not have names
   * matching the given regex. Synthetic methods generated by the compiler are ignored.
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
   * See {@link #methodsShouldNotMatchForClass(DescribedPredicate, String)}, but with
   * {@link Configuration} for customization.
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
   * Adds a rule that all fields must have names matching the given regex. Synthetic fields
   * generated by the compiler are ignored.
   *
   * @param regex the regex that field names must match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsShouldMatch(String regex) {
    return fieldsShouldMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #fieldsShouldMatch(String)}, but with {@link Configuration} for customization.
   *
   * @param regex the regex that field names must match
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().haveNameMatching(regex)
        .as("Fields should have names matching %s".formatted(regex)), configuration));
  }

  /**
   * Adds a rule that fields matching the given criteria must have names matching the given regex.
   * Synthetic fields generated by the compiler are ignored.
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
   * See {@link #fieldsShouldMatch(DescribedPredicate, String)}, but with {@link Configuration} for
   * customization.
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
   * Adds a rule that fields must not have names matching the given regex.
   *
   * @param regex the regex that field names must not match
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer fieldsShouldNotMatch(String regex) {
    return fieldsShouldNotMatch(regex, defaultConfiguration());
  }

  /**
   * See {@link #fieldsShouldNotMatch(String)}, but with {@link Configuration} for customization.
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
   * Adds a rule that fields matching the given criteria must not have names matching the given
   * regex. Synthetic fields generated by the compiler are ignored.
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
   * See {@link #fieldsShouldNotMatch(DescribedPredicate, String)}, but with {@link Configuration}
   * for customization.
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
   * Adds a rule that all fields declared in classes matching the given criteria must have names
   * matching the given regex. Synthetic fields generated by the compiler are ignored.
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
   * See {@link #fieldsShouldMatchForClass(DescribedPredicate, String)}, but with
   * {@link Configuration} for customization.
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
   * Adds a rule that fields declared in classes matching the given criteria must not have names
   * matching the given regex. Synthetic fields generated by the compiler are ignored.
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
   * See {@link #fieldsShouldNotMatchForClass(DescribedPredicate, String)}, but with
   * {@link Configuration} for customization.
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
   * Adds a rule that interfaces must not be prefixed with an uppercase {@code I}, for example
   * {@code IFoo}.
   *
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer interfacesShouldNotHavePrefixI() {
    return interfacesShouldNotHavePrefixI(defaultConfiguration());
  }

  /**
   * See {@link #interfacesShouldNotHavePrefixI()}, but with {@link Configuration} for
   * customization.
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
   * by an uppercase character, for example {@code ICustomer}.
   *
   * @return the condition that detects {@code I}-prefixed interfaces
   */
  private static ArchCondition<JavaClass> notBePrefixedWithI() {
    return new ArchCondition<>("not be prefixed with I") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        String simpleName = javaClass.getSimpleName();

        if (simpleName.length() > 1 && simpleName.charAt(0) == 'I'
            && Character.isUpperCase(simpleName.charAt(1))) {
          events.add(SimpleConditionEvent.violated(javaClass, simpleName));
        }
      }
    };
  }

  /**
   * Adds a rule that constants (static final fields) must follow conventional constant naming, for
   * example {@code UPPER_SNAKE_CASE}, excluding a default set of fields such as
   * {@code serialVersionUID}.
   *
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer constantsShouldFollowConventions() {
    return constantsShouldFollowConventions(DEFAULT_FIELDS_EXCLUDED_FROM_CONSTANT_NAMING,
        defaultConfiguration());
  }

  /**
   * Adds a rule that constants must follow constant naming conventions, excluding the given field
   * names.
   *
   * @param excludedFields field names to exclude from validation, for example
   *     {@code "serialVersionUID"}
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer constantsShouldFollowConventions(Collection<String> excludedFields) {
    return constantsShouldFollowConventions(excludedFields, defaultConfiguration());
  }

  /**
   * See {@link #constantsShouldFollowConventions()}, but with {@link Configuration} for
   * customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer constantsShouldFollowConventions(Configuration configuration) {
    return constantsShouldFollowConventions(DEFAULT_FIELDS_EXCLUDED_FROM_CONSTANT_NAMING,
        configuration);
  }

  /**
   * See {@link #constantsShouldFollowConventions(Collection)}, but with {@link Configuration} for
   * customization.
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
   * Adds a rule that enum constants must follow conventional constant naming, for example
   * {@code UPPER_SNAKE_CASE}.
   *
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer enumConstantsShouldFollowConventions() {
    return enumConstantsShouldFollowConventions(defaultConfiguration());
  }

  /**
   * See {@link #enumConstantsShouldFollowConventions()}, but with {@link Configuration} for
   * customization.
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
   * Adds a rule that methods returning {@code boolean} or {@code Boolean} must start with one of
   * the default prefixes {@code is}, {@code has}, {@code can} or {@code should}.
   *
   * <p>Synthetic methods, {@code equals(Object)} overrides and record component accessors are
   * excluded from this check.</p>
   *
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer booleanMethodsShouldStartWith() {
    return booleanMethodsShouldStartWith(DEFAULT_BOOLEAN_METHOD_PREFIXES, defaultConfiguration());
  }

  /**
   * Adds a rule that methods returning {@code boolean} or {@code Boolean} must start with one of
   * the given prefixes.
   *
   * @param prefixes the allowed method name prefixes, for example {@code "is"} or {@code "has"}
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer booleanMethodsShouldStartWith(Collection<String> prefixes) {
    return booleanMethodsShouldStartWith(prefixes, defaultConfiguration());
  }

  /**
   * See {@link #booleanMethodsShouldStartWith()}, but with {@link Configuration} for customization.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public NamingConfigurer booleanMethodsShouldStartWith(Configuration configuration) {
    return booleanMethodsShouldStartWith(DEFAULT_BOOLEAN_METHOD_PREFIXES, configuration);
  }

  /**
   * See {@link #booleanMethodsShouldStartWith(Collection)}, but with {@link Configuration} for
   * customization.
   *
   * @param prefixes the allowed method name prefixes, for example {@code "is"} or {@code "has"}
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
