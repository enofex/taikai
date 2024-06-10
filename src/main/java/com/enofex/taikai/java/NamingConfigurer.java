package com.enofex.taikai.java;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.regex.Pattern;

public final class NamingConfigurer extends AbstractConfigurer {

  NamingConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public NamingConfigurer classesShouldNotMatch(String regex) {
    return classesShouldNotMatch(regex, null);
  }

  public NamingConfigurer classesShouldNotMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .should().haveNameMatching(regex)
        .as("Classes should not have names matching %s".formatted(regex)), configuration));
  }

  public NamingConfigurer interfacesShouldNotHavePrefixI() {
    return interfacesShouldNotHavePrefixI(null);
  }

  public NamingConfigurer interfacesShouldNotHavePrefixI(Configuration configuration) {
    return addRule(TaikaiRule.of(classes().that().areInterfaces()
        .should(notBePrefixedWithI())
        .as("Interfaces should not be prefixed with I"), configuration));
  }

  private static ArchCondition<JavaClass> notBePrefixedWithI() {
    return new ArchCondition<>("not be prefixed with I.") {
      @Override
      public void check(JavaClass clazz, ConditionEvents events) {
        if (clazz.getSimpleName().startsWith("I") && Character.isUpperCase(
            clazz.getSimpleName().charAt(1))) {
          events.add(SimpleConditionEvent.violated(clazz, clazz.getSimpleName()));
        }
      }
    };
  }

  public NamingConfigurer constantsShouldFollowConvention() {
    return constantsShouldFollowConvention(null);
  }

  public NamingConfigurer constantsShouldFollowConvention(Configuration configuration) {
    return addRule(TaikaiRule.of(fields().that().areFinal().and().areStatic()
        .should(shouldFollowConstantNamingConvention())
        .as("Constants should follow constant naming convention"), configuration));
  }

  private static ArchCondition<JavaField> shouldFollowConstantNamingConvention() {
    return new ArchCondition<>("follow constant naming convention") {

      private static final Pattern CONSTANT_NAME_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

      @Override
      public void check(JavaField field, ConditionEvents events) {
        if (!field.getOwner().isEnum()
            && !CONSTANT_NAME_PATTERN.matcher(field.getName()).matches()) {
          String message = String.format(
              "Constant %s in class %s does not follow the naming convention",
              field.getName(), field.getOwner().getName());
          events.add(SimpleConditionEvent.violated(field, message));
        }
      }
    };
  }
}
