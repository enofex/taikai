package com.enofex.taikai.java;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.internal.ArchConditions.notBePublicButNotStatic;
import static com.enofex.taikai.internal.DescribedPredicates.areFinal;
import static com.enofex.taikai.java.Deprecations.notUseDeprecatedAPIs;
import static com.enofex.taikai.java.HashCodeAndEquals.implementHashCodeAndEquals;
import static com.enofex.taikai.java.NoSystemOutOrErr.notUseSystemOutOrErr;
import static com.enofex.taikai.java.ProtectedMembers.notHaveProtectedMembers;
import static com.enofex.taikai.java.SerialVersionUID.beStaticFinalLong;
import static com.enofex.taikai.java.SerialVersionUID.namedSerialVersionUID;
import static com.enofex.taikai.java.UtilityClasses.havePrivateConstructor;
import static com.enofex.taikai.java.UtilityClasses.utilityClasses;
import static com.tngtech.archunit.lang.conditions.ArchConditions.beFinal;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Customizer;
import java.lang.annotation.Annotation;

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
    return utilityClassesShouldBeFinalAndHavePrivateConstructor(defaultConfiguration());
  }

  public JavaConfigurer utilityClassesShouldBeFinalAndHavePrivateConstructor(
      Configuration configuration) {
    return addRule(TaikaiRule.of(utilityClasses()
        .should(beFinal())
        .andShould(havePrivateConstructor()), configuration));
  }

  public JavaConfigurer methodsShouldNotDeclareGenericExceptions() {
    return methodsShouldNotDeclareGenericExceptions(defaultConfiguration());
  }

  public JavaConfigurer methodsShouldNotDeclareGenericExceptions(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .should().notDeclareThrowableOfType(Exception.class)
        .orShould().notDeclareThrowableOfType(RuntimeException.class)
        .as("Methods should not declare generic Exception or RuntimeException"), configuration));
  }

  public JavaConfigurer methodsShouldNotDeclareException(String regex,
      Class<? extends Throwable> clazz) {
    return methodsShouldNotDeclareException(regex, clazz.getName(), defaultConfiguration());
  }

  public JavaConfigurer methodsShouldNotDeclareException(String regex, String typeName) {
    return methodsShouldNotDeclareException(regex, typeName, defaultConfiguration());
  }

  public JavaConfigurer methodsShouldNotDeclareException(String regex, String typeName,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().haveNameMatching(regex)
            .should().notDeclareThrowableOfType(typeName)
            .as("Methods have name matching %s should not declare %s".formatted(regex, typeName)),
        configuration));
  }

  public JavaConfigurer noUsageOfDeprecatedAPIs() {
    return noUsageOfDeprecatedAPIs(defaultConfiguration());
  }

  public JavaConfigurer noUsageOfDeprecatedAPIs(Configuration configuration) {
    return addRule(TaikaiRule.of(classes().should(notUseDeprecatedAPIs()), configuration));
  }

  public JavaConfigurer classesShouldResideInPackage(String regex, String packageIdentifier) {
    return classesShouldResideInPackage(regex, packageIdentifier, defaultConfiguration());
  }

  public JavaConfigurer classesShouldResideInPackage(String regex, String packageIdentifier,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveNameMatching(regex)
        .should().resideInAPackage(packageIdentifier)
        .as("Classes have name matching %s should reside in package %s".formatted(
            regex, packageIdentifier)), configuration));
  }

  public JavaConfigurer classesShouldResideOutsidePackage(String regex, String packageIdentifier) {
    return classesShouldResideOutsidePackage(regex, packageIdentifier, defaultConfiguration());
  }

  public JavaConfigurer classesShouldResideOutsidePackage(String regex, String packageIdentifier,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveNameMatching(regex)
        .should().resideOutsideOfPackage(packageIdentifier)
        .as("Classes have name matching %s should reside outside package %s".formatted(
            regex, packageIdentifier)), configuration));
  }

  public JavaConfigurer classesShouldBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType) {
    return classesShouldBeAnnotatedWith(regex, annotationType.getName(), defaultConfiguration());
  }

  public JavaConfigurer classesShouldBeAnnotatedWith(String regex, String annotationType) {
    return classesShouldBeAnnotatedWith(regex, annotationType, defaultConfiguration());
  }

  public JavaConfigurer classesShouldBeAnnotatedWith(String regex, String annotationType,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveNameMatching(regex)
        .should().beMetaAnnotatedWith(annotationType)
        .as("Classes have name matching %s should be annotated with %s".formatted(regex,
            annotationType)), configuration));
  }

  public JavaConfigurer classesShouldNotBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType) {
    return classesShouldNotBeAnnotatedWith(regex, annotationType.getName(), defaultConfiguration());
  }

  public JavaConfigurer classesShouldNotBeAnnotatedWith(String regex, String annotationType) {
    return classesShouldNotBeAnnotatedWith(regex, annotationType, defaultConfiguration());
  }

  public JavaConfigurer classesShouldNotBeAnnotatedWith(String regex, String annotationType,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveNameMatching(regex)
        .should().notBeMetaAnnotatedWith(annotationType)
        .as("Classes have name matching %s should not be annotated with %s".formatted(regex,
            annotationType)), configuration));
  }

  public JavaConfigurer classesAnnotatedWithShouldResideInPackage(
      Class<? extends Annotation> annotationType, String packageIdentifier) {
    return classesAnnotatedWithShouldResideInPackage(annotationType.getName(), packageIdentifier,
        defaultConfiguration());
  }

  public JavaConfigurer classesAnnotatedWithShouldResideInPackage(
      String annotationType, String packageIdentifier) {
    return classesAnnotatedWithShouldResideInPackage(annotationType, packageIdentifier,
        defaultConfiguration());
  }

  public JavaConfigurer classesAnnotatedWithShouldResideInPackage(
      String annotationType, String packageIdentifier, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().areMetaAnnotatedWith(annotationType)
        .should().resideInAPackage(packageIdentifier)
        .as("Classes annotated with %s should reside in package %s".formatted(
            annotationType, packageIdentifier)), configuration));
  }

  public JavaConfigurer classesShouldImplementHashCodeAndEquals() {
    return classesShouldImplementHashCodeAndEquals(defaultConfiguration());
  }

  public JavaConfigurer classesShouldImplementHashCodeAndEquals(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(implementHashCodeAndEquals()), configuration));
  }

  public JavaConfigurer classesShouldBeAssignableTo(String regex, Class<?> clazz) {
    return classesShouldBeAssignableTo(regex, clazz.getName(), defaultConfiguration());
  }

  public JavaConfigurer classesShouldBeAssignableTo(String regex, String typeName) {
    return classesShouldBeAssignableTo(regex, typeName, defaultConfiguration());
  }

  public JavaConfigurer classesShouldBeAssignableTo(String regex, String typeName,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveSimpleNameEndingWith(regex)
        .should().beAssignableTo(typeName)
        .as("Classes have name matching %s should be assignable to %s".formatted(
            regex, typeName)), configuration));
  }

  public JavaConfigurer fieldsShouldNotBePublic() {
    return fieldsShouldNotBePublic(defaultConfiguration());
  }

  public JavaConfigurer fieldsShouldNotBePublic(Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .should(notBePublicButNotStatic()), configuration));
  }

  public JavaConfigurer noUsageOf(Class<?> clazz) {
    return noUsageOf(clazz.getName(), null, defaultConfiguration());
  }

  public JavaConfigurer noUsageOf(Class<?> clazz, String packageIdentifier) {
    return noUsageOf(clazz.getName(), packageIdentifier, defaultConfiguration());
  }

  public JavaConfigurer noUsageOf(Class<?> clazz, Configuration configuration) {
    return noUsageOf(clazz.getName(), null, configuration);
  }

  public JavaConfigurer noUsageOf(String typeName) {
    return noUsageOf(typeName, null, defaultConfiguration());
  }

  public JavaConfigurer noUsageOf(String typeName, String packageIdentifier) {
    return noUsageOf(typeName, packageIdentifier, defaultConfiguration());
  }

  public JavaConfigurer noUsageOf(String typeName, Configuration configuration) {
    return noUsageOf(typeName, null, configuration);
  }

  public JavaConfigurer noUsageOf(String typeName, String packageIdentifier,
      Configuration configuration) {
    if (packageIdentifier != null) {
      return addRule(TaikaiRule.of(noClasses()
          .that().resideInAPackage(packageIdentifier)
          .should().dependOnClassesThat().areAssignableTo(typeName)
          .as("Classes %s reside in %s should not be used".formatted(
              typeName, packageIdentifier)), configuration));
    }

    return addRule(TaikaiRule.of(noClasses()
        .should().dependOnClassesThat().areAssignableTo(typeName)
        .as("Classes %s should not be used".formatted(typeName)), configuration));
  }

  public JavaConfigurer noUsageOfSystemOutOrErr() {
    return noUsageOfSystemOutOrErr(defaultConfiguration());
  }

  public JavaConfigurer noUsageOfSystemOutOrErr(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(notUseSystemOutOrErr()), configuration));
  }

  public JavaConfigurer finalClassesShouldNotHaveProtectedMembers() {
    return finalClassesShouldNotHaveProtectedMembers(defaultConfiguration());
  }

  public JavaConfigurer finalClassesShouldNotHaveProtectedMembers(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(areFinal())
        .should(notHaveProtectedMembers())
        .as("Final classes should not have protected members"), configuration));
  }

  public JavaConfigurer serialVersionUIDFieldsShouldBeStaticFinalLong() {
    return serialVersionUIDFieldsShouldBeStaticFinalLong(defaultConfiguration());
  }

  public JavaConfigurer serialVersionUIDFieldsShouldBeStaticFinalLong(Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that(namedSerialVersionUID())
        .should(beStaticFinalLong())
        .as("serialVersionUID should be static final long"), configuration));
  }

  @Override
  public void disable() {
    disable(ImportsConfigurer.class);
    disable(NamingConfigurer.class);
  }
}