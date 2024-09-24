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

public class NamingConfigurer extends AbstractConfigurer {

  private static final String PACKAGE_NAME_REGEX = "^[a-z_]+(\\.[a-z_][a-z0-9_]*)*$";

  NamingConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public NamingConfigurer packagesShouldMatchDefault() {
    return packagesShouldMatch(PACKAGE_NAME_REGEX, defaultConfiguration());
  }

  public NamingConfigurer packagesShouldMatchDefault(Configuration configuration) {
    return packagesShouldMatch(PACKAGE_NAME_REGEX, configuration);
  }

  public NamingConfigurer packagesShouldMatch(String regex) {
    return packagesShouldMatch(regex, defaultConfiguration());
  }

  public NamingConfigurer packagesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(resideInPackageWithProperNamingConvention(regex))
        .as("Packages should have names matching %s".formatted(regex)), configuration));
  }

  public NamingConfigurer classesShouldNotMatch(String regex) {
    return classesShouldNotMatch(regex, defaultConfiguration());
  }

  public NamingConfigurer classesShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .should().haveNameMatching(regex)
        .as("Classes should not have names matching %s".formatted(regex)), configuration));
  }

  public NamingConfigurer classesAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex) {
    return classesAnnotatedWithShouldMatch(annotationType.getName(), regex, defaultConfiguration());
  }

  public NamingConfigurer classesAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex, Configuration configuration) {
    return classesAnnotatedWithShouldMatch(annotationType.getName(), regex, configuration);
  }

  public NamingConfigurer classesAnnotatedWithShouldMatch(String annotationType, String regex) {
    return classesAnnotatedWithShouldMatch(annotationType, regex, defaultConfiguration());
  }

  public NamingConfigurer classesAnnotatedWithShouldMatch(String annotationType, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().areMetaAnnotatedWith(annotationType)
        .should().haveNameMatching(regex)
        .as("Classes annotated with %s should have names matching %s".formatted(
            annotationType, regex)), configuration));
  }

  public NamingConfigurer methodsAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex) {
    return methodsAnnotatedWithShouldMatch(annotationType.getName(), regex, defaultConfiguration());
  }

  public NamingConfigurer methodsAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex, Configuration configuration) {
    return methodsAnnotatedWithShouldMatch(annotationType.getName(), regex, configuration);
  }

  public NamingConfigurer methodsAnnotatedWithShouldMatch(
      String annotationType, String regex) {
    return methodsAnnotatedWithShouldMatch(annotationType, regex, defaultConfiguration());
  }

  public NamingConfigurer methodsAnnotatedWithShouldMatch(String annotationType, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().areMetaAnnotatedWith(annotationType)
        .should().haveNameMatching(regex)
        .as("Methods annotated with %s should have names matching %s".formatted(
            annotationType, regex)), configuration));
  }

  public NamingConfigurer methodsShouldNotMatch(String regex) {
    return methodsShouldNotMatch(regex, defaultConfiguration());
  }

  public NamingConfigurer methodsShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noMethods()
        .should().haveNameMatching(regex)
        .as("Methods should not have names matching %s".formatted(regex)), configuration));
  }

  public NamingConfigurer fieldsAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex) {
    return fieldsAnnotatedWithShouldMatch(annotationType.getName(), regex, defaultConfiguration());
  }

  public NamingConfigurer fieldsAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex, Configuration configuration) {
    return fieldsAnnotatedWithShouldMatch(annotationType.getName(), regex, configuration);
  }

  public NamingConfigurer fieldsAnnotatedWithShouldMatch(String annotationType, String regex) {
    return fieldsAnnotatedWithShouldMatch(annotationType, regex, defaultConfiguration());
  }

  public NamingConfigurer fieldsAnnotatedWithShouldMatch(String annotationType, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that().areMetaAnnotatedWith(annotationType)
        .should().haveNameMatching(regex)
        .as("Fields annotated with %s should have names matching %s".formatted(
            annotationType, regex)), configuration));
  }

  public NamingConfigurer fieldsShouldMatch(String typeName, String regex) {
    return fieldsShouldMatch(typeName, regex, defaultConfiguration());
  }

  public NamingConfigurer fieldsShouldMatch(Class<?> clazz, String regex) {
    return fieldsShouldMatch(clazz.getName(), regex, defaultConfiguration());
  }

  public NamingConfigurer fieldsShouldMatch(Class<?> clazz, String regex,
      Configuration configuration) {
    return fieldsShouldMatch(clazz.getName(), regex, configuration);
  }

  public NamingConfigurer fieldsShouldMatch(String typeName, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
            .that().haveRawType(typeName)
            .should().haveNameMatching(regex)
            .as("Fields of type %s should have names matching %s".formatted(typeName, regex)),
        configuration));
  }

  public NamingConfigurer fieldsShouldNotMatch(String regex) {
    return fieldsShouldNotMatch(regex, defaultConfiguration());
  }

  public NamingConfigurer fieldsShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noFields()
        .should().haveNameMatching(regex)
        .as("Fields should not have names matching %s".formatted(regex)), configuration));
  }

  public NamingConfigurer interfacesShouldNotHavePrefixI() {
    return interfacesShouldNotHavePrefixI(defaultConfiguration());
  }

  public NamingConfigurer interfacesShouldNotHavePrefixI(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().areInterfaces()
        .should(notBePrefixedWithI())
        .as("Interfaces should not be prefixed with I"), configuration));
  }

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

  public NamingConfigurer constantsShouldFollowConventions() {
    return constantsShouldFollowConventions(defaultConfiguration());
  }

  public NamingConfigurer constantsShouldFollowConventions(Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that().areFinal().and().areStatic()
        .should(shouldFollowConstantNamingConventions())
        .as("Constants should follow constant naming conventions"), configuration));
  }

  public static final class Disableable extends NamingConfigurer implements DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public NamingConfigurer disable() {
      disable(NamingConfigurer.class);

      return this;
    }
  }
}
