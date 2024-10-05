package com.enofex.taikai.java;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.internal.ArchConditions.hasFieldModifiers;
import static com.enofex.taikai.internal.ArchConditions.notBePublicButNotStatic;
import static com.enofex.taikai.internal.DescribedPredicates.annotatedWithAll;
import static com.enofex.taikai.internal.DescribedPredicates.areFinal;
import static com.enofex.taikai.java.Deprecations.notUseDeprecatedAPIs;
import static com.enofex.taikai.java.HashCodeAndEquals.implementHashCodeAndEquals;
import static com.enofex.taikai.java.NoSystemOutOrErr.notUseSystemOutOrErr;
import static com.enofex.taikai.java.ProtectedMembers.notHaveProtectedMembers;
import static com.enofex.taikai.java.SerialVersionUID.beStaticFinalLong;
import static com.enofex.taikai.java.SerialVersionUID.namedSerialVersionUID;
import static com.enofex.taikai.java.UtilityClasses.havePrivateConstructor;
import static com.enofex.taikai.java.UtilityClasses.utilityClasses;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
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
import com.enofex.taikai.configures.DisableableConfigurer;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Collectors;

public class JavaConfigurer extends AbstractConfigurer {

  public JavaConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public Disableable imports(Customizer<ImportsConfigurer.Disableable> customizer) {
    return customizer(customizer, () -> new ImportsConfigurer.Disableable(configurerContext()));
  }

  public Disableable naming(Customizer<NamingConfigurer.Disableable> customizer) {
    return customizer(customizer, () -> new NamingConfigurer.Disableable(configurerContext()));
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

  public JavaConfigurer methodsShouldNotDeclareException(String regex,
      Class<? extends Throwable> clazz, Configuration configuration) {
    return methodsShouldNotDeclareException(regex, clazz.getName(), configuration);
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

  public JavaConfigurer methodsShouldBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType) {
    return methodsShouldBeAnnotatedWith(regex, annotationType.getName(), defaultConfiguration());
  }

  public JavaConfigurer methodsShouldBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType, Configuration configuration) {
    return methodsShouldBeAnnotatedWith(regex, annotationType.getName(), configuration);
  }

  public JavaConfigurer methodsShouldBeAnnotatedWith(String regex, String annotationType) {
    return methodsShouldBeAnnotatedWith(regex, annotationType, defaultConfiguration());
  }

  public JavaConfigurer methodsShouldBeAnnotatedWith(String regex, String annotationType,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().haveNameMatching(regex)
        .should().beMetaAnnotatedWith(annotationType)
        .as("Methods have name matching %s should be annotated with %s".formatted(regex,
            annotationType)), configuration));
  }

  public JavaConfigurer methodsShouldBeAnnotatedWithAll(Class<? extends Annotation> annotationType,
      Collection<Class<? extends Annotation>> requiredAnnotationTypes) {
    return methodsShouldBeAnnotatedWithAll(annotationType.getName(),
        requiredAnnotationTypes.stream().map(Class::getName).toList(), defaultConfiguration());
  }

  public JavaConfigurer methodsShouldBeAnnotatedWithAll(Class<? extends Annotation> annotationType,
      Collection<Class<? extends Annotation>> requiredAnnotationTypes,
      Configuration configuration) {
    return methodsShouldBeAnnotatedWithAll(annotationType.getName(),
        requiredAnnotationTypes.stream().map(Class::getName).toList(), configuration);
  }

  public JavaConfigurer methodsShouldBeAnnotatedWithAll(String annotationType,
      Collection<String> requiredAnnotationTypes) {
    return methodsShouldBeAnnotatedWithAll(annotationType, requiredAnnotationTypes,
        defaultConfiguration());
  }

  public JavaConfigurer methodsShouldBeAnnotatedWithAll(String annotationType,
      Collection<String> requiredAnnotationTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().areMetaAnnotatedWith(annotationType)
            .should(be(annotatedWithAll(requiredAnnotationTypes, true)))
            .as("Methods annotated with %s should be annotated with %s".formatted(
                annotationType, String.join(", ", requiredAnnotationTypes))),
        configuration));
  }

  public JavaConfigurer noUsageOfDeprecatedAPIs() {
    return noUsageOfDeprecatedAPIs(defaultConfiguration());
  }

  public JavaConfigurer noUsageOfDeprecatedAPIs(Configuration configuration) {
    return addRule(TaikaiRule.of(classes().should(notUseDeprecatedAPIs()), configuration));
  }

  public JavaConfigurer classesShouldResideInPackage(String packageIdentifier) {
    return classesShouldResideInPackage(packageIdentifier, defaultConfiguration());
  }

  public JavaConfigurer classesShouldResideInPackage(String packageIdentifier,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .should().resideInAPackage(packageIdentifier)
            .as("Package names should match %s".formatted(packageIdentifier)),
        configuration));
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

  public JavaConfigurer classesShouldBeAnnotatedWithAll(Class<? extends Annotation> annotationType,
      Collection<Class<? extends Annotation>> requiredAnnotationTypes) {
    return classesShouldBeAnnotatedWithAll(annotationType.getName(),
        requiredAnnotationTypes.stream().map(Class::getName).toList(), defaultConfiguration());
  }

  public JavaConfigurer classesShouldBeAnnotatedWithAll(Class<? extends Annotation> annotationType,
      Collection<Class<? extends Annotation>> requiredAnnotationTypes,
      Configuration configuration) {
    return classesShouldBeAnnotatedWithAll(annotationType.getName(),
        requiredAnnotationTypes.stream().map(Class::getName).toList(), configuration);
  }

  public JavaConfigurer classesShouldBeAnnotatedWithAll(String annotationType,
      Collection<String> requiredAnnotationTypes) {
    return classesShouldBeAnnotatedWithAll(annotationType, requiredAnnotationTypes,
        defaultConfiguration());
  }

  public JavaConfigurer classesShouldBeAnnotatedWithAll(String annotationType,
      Collection<String> requiredAnnotationTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areMetaAnnotatedWith(annotationType)
            .should(be(annotatedWithAll(requiredAnnotationTypes, true)))
            .as("Classes annotated with %s should be annotated with %s".formatted(
                annotationType, String.join(", ", requiredAnnotationTypes))),
        configuration));
  }

  public JavaConfigurer classesShouldBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType) {
    return classesShouldBeAnnotatedWith(regex, annotationType.getName(), defaultConfiguration());
  }

  public JavaConfigurer classesShouldBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType, Configuration configuration) {
    return classesShouldBeAnnotatedWith(regex, annotationType.getName(), configuration);
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

  public JavaConfigurer classesShouldNotBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType, Configuration configuration) {
    return classesShouldNotBeAnnotatedWith(regex, annotationType.getName(), configuration);
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
      Class<? extends Annotation> annotationType, String packageIdentifier,
      Configuration configuration) {
    return classesAnnotatedWithShouldResideInPackage(annotationType.getName(), packageIdentifier,
        configuration);
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

  public JavaConfigurer classesShouldBeAssignableTo(String regex, Class<?> clazz,
      Configuration configuration) {
    return classesShouldBeAssignableTo(regex, clazz.getName(), configuration);
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

  public JavaConfigurer classesShouldImplement(String regex, Class<?> clazz) {
    return classesShouldImplement(regex, clazz.getName(), defaultConfiguration());
  }

  public JavaConfigurer classesShouldImplement(String regex, Class<?> clazz,
      Configuration configuration) {
    return classesShouldImplement(regex, clazz.getName(), configuration);
  }

  public JavaConfigurer classesShouldImplement(String regex, String typeName) {
    return classesShouldImplement(regex, typeName, defaultConfiguration());
  }

  public JavaConfigurer classesShouldImplement(String regex, String typeName,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveSimpleNameEndingWith(regex)
        .should().implement(typeName)
        .as("Classes have name matching %s should implement %s".formatted(
            regex, typeName)), configuration));
  }

  public JavaConfigurer fieldsShouldNotBePublic() {
    return fieldsShouldNotBePublic(defaultConfiguration());
  }

  public JavaConfigurer fieldsShouldNotBePublic(Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .should(notBePublicButNotStatic()), configuration));
  }

  public JavaConfigurer fieldsShouldHaveModifiers(String regex,
      Collection<JavaModifier> requiredModifiers) {
    return fieldsShouldHaveModifiers(regex, requiredModifiers, defaultConfiguration());
  }

  public JavaConfigurer fieldsShouldHaveModifiers(String regex,
      Collection<JavaModifier> requiredModifiers,
      Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
            .that().haveNameMatching(regex)
            .should(hasFieldModifiers(requiredModifiers))
            .as("Fields have name matching %s should have all of this modifiers %s".formatted(
                regex,
                requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  public JavaConfigurer noUsageOf(Class<?> clazz) {
    return noUsageOf(clazz.getName(), null, defaultConfiguration());
  }

  public JavaConfigurer noUsageOf(Class<?> clazz, String packageIdentifier) {
    return noUsageOf(clazz.getName(), packageIdentifier, defaultConfiguration());
  }

  public JavaConfigurer noUsageOf(Class<?> clazz, String packageIdentifier,
      Configuration configuration) {
    return noUsageOf(clazz.getName(), packageIdentifier, configuration);
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

  public static final class Disableable extends JavaConfigurer implements DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public JavaConfigurer disable() {
      disable(JavaConfigurer.class);
      disable(ImportsConfigurer.class);
      disable(NamingConfigurer.class);

      return this;
    }
  }
}