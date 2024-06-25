package com.enofex.taikai.java;

import static com.enofex.taikai.java.ConstantNaming.shouldFollowConstantNamingConventions;
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
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.lang.annotation.Annotation;

public final class NamingConfigurer extends AbstractConfigurer {

  NamingConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public NamingConfigurer packagesShouldMatch(String regex) {
    return packagesShouldMatch(regex, null);
  }

  public NamingConfigurer packagesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .should().resideInAPackage(regex)
            .as("Package names should match %s".formatted(regex)),
        configuration));
  }

  public NamingConfigurer classesShouldNotMatch(String regex) {
    return classesShouldNotMatch(regex, null);
  }

  public NamingConfigurer classesShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .should().haveNameMatching(regex)
        .as("Classes should not have names matching %s".formatted(regex)), configuration));
  }

  public NamingConfigurer classesAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex) {
    return classesAnnotatedWithShouldMatch(annotationType, regex, null);
  }

  public NamingConfigurer classesAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().areMetaAnnotatedWith(annotationType)
        .should().haveNameMatching(regex)
        .as("Classes annotated with %s should have names matching %s".formatted(
            annotationType.getName(), regex)), configuration));
  }

  public NamingConfigurer classesAnnotatedWithShouldMatch(String annotationType, String regex) {
    return classesAnnotatedWithShouldMatch(annotationType, regex, null);
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
    return methodsAnnotatedWithShouldMatch(annotationType, regex, null);
  }

  public NamingConfigurer methodsAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().areMetaAnnotatedWith(annotationType)
        .should().haveNameMatching(regex)
        .as("Methods annotated with %s should have names matching %s".formatted(
            annotationType.getName(), regex)), configuration));
  }

  public NamingConfigurer methodsAnnotatedWithShouldMatch(
      String annotationType, String regex) {
    return methodsAnnotatedWithShouldMatch(annotationType, regex, null);
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
    return methodsShouldNotMatch(regex, null);
  }

  public NamingConfigurer methodsShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noMethods()
        .should().haveNameMatching(regex)
        .as("Methods should not have names matching %s".formatted(regex)), configuration));
  }

  public NamingConfigurer fieldsAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex) {
    return fieldsAnnotatedWithShouldMatch(annotationType, regex, null);
  }

  public NamingConfigurer fieldsAnnotatedWithShouldMatch(
      Class<? extends Annotation> annotationType, String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that().areMetaAnnotatedWith(annotationType)
        .should().haveNameMatching(regex)
        .as("Fields annotated with %s should have names matching %s".formatted(
            annotationType.getName(), regex)), configuration));
  }

  public NamingConfigurer fieldsAnnotatedWithShouldMatch(String annotationType, String regex) {
    return fieldsAnnotatedWithShouldMatch(annotationType, regex, null);
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
    return fieldsShouldMatch(typeName, regex, null);
  }

  public NamingConfigurer fieldsShouldMatch(String typeName, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
            .that().haveRawType(typeName)
            .should().haveNameMatching(regex)
            .as("Fields of type %s should have names matching %s".formatted(typeName, regex)),
        configuration));
  }

  public NamingConfigurer fieldsShouldMatch(Class<?> clazz, String regex) {
    return fieldsShouldMatch(clazz, regex, null);
  }

  public NamingConfigurer fieldsShouldMatch(Class<?> clazz, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
            .that().haveRawType(clazz)
            .should().haveNameMatching(regex)
            .as("Fields of type %s should have names matching %s".formatted(clazz, regex)),
        configuration));
  }

  public NamingConfigurer fieldsShouldNotMatch(String regex) {
    return fieldsShouldNotMatch(regex, null);
  }

  public NamingConfigurer fieldsShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noFields()
        .should().haveNameMatching(regex)
        .as("Fields should not have names matching %s".formatted(regex)), configuration));
  }

  public NamingConfigurer interfacesShouldNotHavePrefixI() {
    return interfacesShouldNotHavePrefixI(null);
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
    return constantsShouldFollowConventions(null);
  }

  public NamingConfigurer constantsShouldFollowConventions(Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that().areFinal().and().areStatic()
        .should(shouldFollowConstantNamingConventions())
        .as("Constants should follow constant naming conventions"), configuration));
  }
}
