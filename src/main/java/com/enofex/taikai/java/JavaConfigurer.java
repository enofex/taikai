package com.enofex.taikai.java;

import static com.enofex.taikai.java.Deprecations.notUseDeprecatedAPIs;
import static com.enofex.taikai.java.FieldsShouldNotBePublic.notBePublic;
import static com.enofex.taikai.java.HashCodeAndEquals.implementHashCodeAndEquals;
import static com.enofex.taikai.java.UtilityClasses.beFinal;
import static com.enofex.taikai.java.UtilityClasses.havePrivateConstructor;
import static com.enofex.taikai.java.UtilityClasses.utilityClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;

public final class JavaConfigurer extends AbstractConfigurer {

  public JavaConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public JavaConfigurer imports(Customizer<ImportsConfigurer> customizer) {
    return customizer(customizer, () -> new ImportsConfigurer(configurerContext()));
  }

  public JavaConfigurer naming(Customizer<NamingConfigurer> customizer) {
    return customizer(customizer, () -> new NamingConfigurer(configurerContext()));
  }

  public JavaConfigurer utilityClassesShouldBeFinalAndHavePrivateConstructor() {
    return utilityClassesShouldBeFinalAndHavePrivateConstructor(null);
  }

  public JavaConfigurer utilityClassesShouldBeFinalAndHavePrivateConstructor(
      Configuration configuration) {
    return addRule(TaikaiRule.of(utilityClasses()
        .should(beFinal())
        .andShould(havePrivateConstructor()), configuration));
  }

  public JavaConfigurer methodsShouldNotThrowGenericException() {
    return methodsShouldNotThrowGenericException(null);
  }

  public JavaConfigurer methodsShouldNotThrowGenericException(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .should().notDeclareThrowableOfType(Exception.class)
        .orShould().notDeclareThrowableOfType(RuntimeException.class)
        .as("Methods should not throw generic Exception or RuntimeException"), configuration));
  }

  public JavaConfigurer noUsageOfDeprecatedAPIs() {
    return noUsageOfDeprecatedAPIs(null);
  }

  public JavaConfigurer noUsageOfDeprecatedAPIs(Configuration configuration) {
    return addRule(TaikaiRule.of(classes().should(notUseDeprecatedAPIs()), configuration));
  }

  public JavaConfigurer classesShouldImplementHashCodeAndEquals() {
    return classesShouldImplementHashCodeAndEquals(null);
  }

  public JavaConfigurer classesShouldImplementHashCodeAndEquals(Configuration configuration) {
    return addRule(TaikaiRule.of(classes().should(implementHashCodeAndEquals()), configuration));
  }

  public JavaConfigurer fieldsShouldNotBePublic() {
    return fieldsShouldNotBePublic(null);
  }

  public JavaConfigurer fieldsShouldNotBePublic(Configuration configuration) {
    return addRule(TaikaiRule.of(fields().should(notBePublic()), configuration));
  }

  @Override
  public void disable() {
    disable(ImportsConfigurer.class);
    disable(NamingConfigurer.class);
  }
}