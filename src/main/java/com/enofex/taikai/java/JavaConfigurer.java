package com.enofex.taikai.java;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.internal.ArchConditions.hasClassModifiers;
import static com.enofex.taikai.internal.ArchConditions.hasFieldModifiers;
import static com.enofex.taikai.internal.ArchConditions.hasMethodsModifiers;
import static com.enofex.taikai.internal.ArchConditions.notBePublicUnlessStatic;
import static com.enofex.taikai.internal.ArchConditions.notHasMethodModifiers;
import static com.enofex.taikai.internal.DescribedPredicates.annotatedWithAll;
import static com.enofex.taikai.internal.DescribedPredicates.areFinal;
import static com.enofex.taikai.java.Deprecations.notUseDeprecatedAPIs;
import static com.enofex.taikai.java.HashCodeAndEquals.implementHashCodeAndEquals;
import static com.enofex.taikai.java.MaxMethodParameters.notExceedMaxParameters;
import static com.enofex.taikai.java.NoSystemOutOrErr.notUseSystemOutOrErr;
import static com.enofex.taikai.java.ProtectedMembers.notHaveProtectedMembers;
import static com.enofex.taikai.java.SerialVersionUID.beStaticFinalLong;
import static com.enofex.taikai.java.SerialVersionUID.namedSerialVersionUID;
import static com.enofex.taikai.java.UtilityClasses.havePrivateConstructor;
import static com.enofex.taikai.java.UtilityClasses.utilityClasses;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchConditions.beFinal;
import static com.tngtech.archunit.lang.conditions.ArchConditions.haveNameMatching;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
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
import org.jspecify.annotations.Nullable;

/**
 * Configures and enforces general Java code quality and design rules using
 * {@link com.tngtech.archunit ArchUnit} through the Taikai framework.
 *
 * <p>This configurer provides a rich set of predefined rules that validate
 * Java-specific conventions, patterns, and constraints — ensuring
 * maintainability, consistency, and adherence to best practices across the codebase.</p>
 *
 * <p>Common validations include:</p>
 * <ul>
 *   <li>Utility class design (must be {@code final} with a private constructor)</li>
 *   <li>Method and field modifier enforcement</li>
 *   <li>Annotation consistency and exclusivity checks</li>
 *   <li>Prohibition of generic exception declarations</li>
 *   <li>Restriction of deprecated API and {@code System.out}/{@code System.err} usage</li>
 *   <li>Package and dependency restrictions</li>
 *   <li>Ensuring {@code serialVersionUID} correctness</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.example.project")
 *     .java(java -> java
 *         .utilityClassesShouldBeFinalAndHavePrivateConstructor()
 *         .methodsShouldNotDeclareGenericExceptions()
 *         .noUsageOfDeprecatedAPIs()
 *         .classesShouldImplementHashCodeAndEquals()
 *         .fieldsShouldNotBePublic()
 *         .finalClassesShouldNotHaveProtectedMembers()
 *     );
 * }</pre>
 *
 * <p>Each rule can be customized via {@link com.enofex.taikai.TaikaiRule.Configuration}
 * or composed through {@link com.enofex.taikai.configures.Customizer} for finer control.
 * The {@link com.enofex.taikai.configures.ConfigurerContext} manages the shared configuration
 * between related configurers (such as {@link ImportsConfigurer} and {@link NamingConfigurer}).</p>
 *
 * @see com.enofex.taikai.TaikaiRule
 * @see com.enofex.taikai.configures.AbstractConfigurer
 * @see com.enofex.taikai.configures.Customizer
 * @see com.enofex.taikai.configures.ConfigurerContext
 * @see com.tngtech.archunit.lang.ArchRule
 */
public class JavaConfigurer extends AbstractConfigurer {

  JavaConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public Disableable imports(Customizer<ImportsConfigurer.Disableable> customizer) {
    return customizer(customizer, () -> new ImportsConfigurer.Disableable(configurerContext()));
  }

  public Disableable naming(Customizer<NamingConfigurer.Disableable> customizer) {
    return customizer(customizer, () -> new NamingConfigurer.Disableable(configurerContext()));
  }

  /**
   * Adds a rule enforcing that utility classes must be {@code final}
   * and have a private constructor.
   *
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer utilityClassesShouldBeFinalAndHavePrivateConstructor() {
    return utilityClassesShouldBeFinalAndHavePrivateConstructor(defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that utility classes must be {@code final}
   * and have a private constructor, using a custom {@link Configuration}.
   *
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer utilityClassesShouldBeFinalAndHavePrivateConstructor(
      Configuration configuration) {
    return addRule(TaikaiRule.of(utilityClasses()
        .should(beFinal())
        .andShould(havePrivateConstructor())
        .as("Utility classes should be final and have a private constructor"), configuration));
  }

  /**
   * Adds a rule preventing methods from declaring {@code Exception}
   * or {@code RuntimeException}.
   *
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldNotDeclareGenericExceptions() {
    return methodsShouldNotDeclareGenericExceptions(defaultConfiguration());
  }

  /**
   * Adds a rule preventing methods from declaring {@code Exception}
   * or {@code RuntimeException}, using a custom {@link Configuration}.
   *
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldNotDeclareGenericExceptions(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .should().notDeclareThrowableOfType(Exception.class)
        .orShould().notDeclareThrowableOfType(RuntimeException.class)
        .as("Methods should not declare generic Exception or RuntimeException"), configuration));
  }

  /**
   * Adds a rule preventing methods whose names match the given regex
   * from declaring a specific exception type.
   *
   * @param regex a regex for matching method names
   * @param clazz the exception type
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldNotDeclareException(String regex,
      Class<? extends Throwable> clazz) {
    return methodsShouldNotDeclareException(regex, clazz.getName(), defaultConfiguration());
  }

  /**
   * Adds a rule preventing methods whose names match the given regex
   * from declaring a specific exception type, with a custom configuration.
   *
   * @param regex a regex for matching method names
   * @param clazz the exception type
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldNotDeclareException(String regex,
      Class<? extends Throwable> clazz, Configuration configuration) {
    return methodsShouldNotDeclareException(regex, clazz.getName(), configuration);
  }

  /**
   * Adds a rule preventing methods whose names match the given regex
   * from declaring a specific exception type by name.
   *
   * @param regex a regex for matching method names
   * @param typeName the exception type name
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldNotDeclareException(String regex, String typeName) {
    return methodsShouldNotDeclareException(regex, typeName, defaultConfiguration());
  }

  /**
   * Adds a rule preventing methods whose names match the given regex
   * from declaring a specific exception type by name, with a custom configuration.
   *
   * @param regex a regex for matching method names
   * @param typeName the exception type name
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldNotDeclareException(String regex, String typeName,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().haveNameMatching(regex)
            .should().notDeclareThrowableOfType(typeName)
            .as("Methods have name matching %s should not declare %s".formatted(regex, typeName)),
        configuration));
  }

  /**
   * Adds a rule enforcing that methods with names matching a regex
   * must be annotated with a specific annotation.
   *
   * @param regex the regex for method names
   * @param annotationType the required annotation class
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType) {
    return methodsShouldBeAnnotatedWith(regex, annotationType.getName(), defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that methods with names matching a regex
   * must be annotated with a specific annotation, with custom configuration.
   *
   * @param regex the regex for method names
   * @param annotationType the required annotation class
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType, Configuration configuration) {
    return methodsShouldBeAnnotatedWith(regex, annotationType.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that methods with names matching a regex
   * must be annotated with a specific annotation by name.
   *
   * @param regex the regex for method names
   * @param annotationType the required annotation type name
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldBeAnnotatedWith(String regex, String annotationType) {
    return methodsShouldBeAnnotatedWith(regex, annotationType, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that methods with names matching a regex
   * must be annotated with a specific annotation by name, using a custom configuration.
   *
   * @param regex the regex for method names
   * @param annotationType the required annotation type name
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldBeAnnotatedWith(String regex, String annotationType,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().haveNameMatching(regex)
        .should().beMetaAnnotatedWith(annotationType)
        .as("Methods have name matching %s should be annotated with %s".formatted(regex,
            annotationType)), configuration));
  }

  /**
   * Adds a rule enforcing that methods annotated with a specific annotation
   * must also be annotated with all given annotations.
   *
   * @param annotationType the base annotation type
   * @param requiredAnnotationTypes the required annotations that must coexist
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldBeAnnotatedWithAll(Class<? extends Annotation> annotationType,
      Collection<Class<? extends Annotation>> requiredAnnotationTypes) {
    return methodsShouldBeAnnotatedWithAll(annotationType.getName(),
        requiredAnnotationTypes.stream().map(Class::getName).toList(), defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that methods annotated with a specific annotation
   * must also be annotated with all given annotations, using a custom configuration.
   *
   * @param annotationType the base annotation type
   * @param requiredAnnotationTypes the required annotations that must coexist
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldBeAnnotatedWithAll(Class<? extends Annotation> annotationType,
      Collection<Class<? extends Annotation>> requiredAnnotationTypes,
      Configuration configuration) {
    return methodsShouldBeAnnotatedWithAll(annotationType.getName(),
        requiredAnnotationTypes.stream().map(Class::getName).toList(), configuration);
  }

  /**
   * Adds a rule enforcing that methods annotated with a specific annotation (by name)
   * must also be annotated with all given annotations.
   *
   * @param annotationType the base annotation type name
   * @param requiredAnnotationTypes a collection of required annotation type names
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldBeAnnotatedWithAll(String annotationType,
      Collection<String> requiredAnnotationTypes) {
    return methodsShouldBeAnnotatedWithAll(annotationType, requiredAnnotationTypes,
        defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that methods annotated with a specific annotation (by name)
   * must also be annotated with all given annotations, using a custom configuration.
   *
   * @param annotationType the base annotation type name
   * @param requiredAnnotationTypes a collection of required annotation type names
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldBeAnnotatedWithAll(String annotationType,
      Collection<String> requiredAnnotationTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().areMetaAnnotatedWith(annotationType)
            .should(be(annotatedWithAll(requiredAnnotationTypes, true)))
            .as("Methods annotated with %s should be annotated with %s".formatted(
                annotationType, String.join(", ", requiredAnnotationTypes))),
        configuration));
  }

  /**
   * Adds a rule enforcing that methods annotated with one annotation
   * must not be annotated with another conflicting annotation.
   *
   * @param annotationType the base annotation
   * @param notAnnotationType the disallowed annotation
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsAnnotatedWithShouldNotBeAnnotatedWith(Class<? extends Annotation> annotationType,
      Class<? extends Annotation> notAnnotationType) {
    return methodsAnnotatedWithShouldNotBeAnnotatedWith(annotationType.getName(), notAnnotationType.getName(), defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that methods annotated with one annotation
   * must not be annotated with another conflicting annotation, using a custom configuration.
   *
   * @param annotationType the base annotation
   * @param notAnnotationType the disallowed annotation
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsAnnotatedWithShouldNotBeAnnotatedWith(Class<? extends Annotation> annotationType,
      Class<? extends Annotation> notAnnotationType, Configuration configuration) {
    return methodsAnnotatedWithShouldNotBeAnnotatedWith(annotationType.getName(), notAnnotationType.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that methods annotated with one annotation (by name)
   * must not be annotated with another.
   *
   * @param annotationType the base annotation type name
   * @param notAnnotationType the disallowed annotation type name
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsAnnotatedWithShouldNotBeAnnotatedWith(String annotationType, String notAnnotationType) {
    return methodsAnnotatedWithShouldNotBeAnnotatedWith(annotationType, notAnnotationType, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that methods annotated with one annotation (by name)
   * must not be annotated with another, using a custom configuration.
   *
   * @param annotationType the base annotation type name
   * @param notAnnotationType the disallowed annotation type name
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsAnnotatedWithShouldNotBeAnnotatedWith(String annotationType, String notAnnotationType,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().areAnnotatedWith(annotationType)
        .should().notBeMetaAnnotatedWith(notAnnotationType)
        .as("Methods annotated with %s should not be annotated with %s".formatted(annotationType,
            notAnnotationType)), configuration));
  }

  /**
   * Adds a rule enforcing that methods annotated with a given annotation type
   * must have all the specified modifiers.
   *
   * @param annotationType the annotation type to check
   * @param requiredModifiers the required {@link JavaModifier}s
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsAnnotatedWithShouldHaveModifiers(
      Class<? extends Annotation> annotationType,
      Collection<JavaModifier> requiredModifiers) {
    return methodsAnnotatedWithShouldHaveModifiers(annotationType.getName(), requiredModifiers,
        defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that methods annotated with a given annotation type
   * must have all the specified modifiers, using a custom configuration.
   *
   * @param annotationType the annotation type to check
   * @param requiredModifiers the required {@link JavaModifier}s
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsAnnotatedWithShouldHaveModifiers(
      Class<? extends Annotation> annotationType,
      Collection<JavaModifier> requiredModifiers, Configuration configuration) {
    return methodsAnnotatedWithShouldHaveModifiers(annotationType.getName(), requiredModifiers,
        configuration);
  }

  /**
   * Adds a rule enforcing that methods annotated with an annotation whose name matches
   * the given type name must have all the specified modifiers.
   *
   * @param annotationType the annotation type name to check
   * @param requiredModifiers the required {@link JavaModifier}s
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsAnnotatedWithShouldHaveModifiers(String annotationType,
      Collection<JavaModifier> requiredModifiers) {
    return methodsAnnotatedWithShouldHaveModifiers(annotationType, requiredModifiers,
        defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that methods annotated with an annotation whose name matches
   * the given type name must have all the specified modifiers, using a custom configuration.
   *
   * @param annotationType the annotation type name to check
   * @param requiredModifiers the required {@link JavaModifier}s
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsAnnotatedWithShouldHaveModifiers(String annotationType,
      Collection<JavaModifier> requiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().areAnnotatedWith(annotationType)
            .should(hasMethodsModifiers(requiredModifiers))
            .as("Methods annotated with %s should have all of this modifiers %s".formatted(
                annotationType,
                requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that methods annotated with a given annotation type
   * must not have all the specified modifiers.
   *
   * @param annotationType the annotation type to check
   * @param notRequiredModifiers the not required {@link JavaModifier}s
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsAnnotatedWithShouldNotHaveModifiers(
      Class<? extends Annotation> annotationType,
      Collection<JavaModifier> notRequiredModifiers) {
    return methodsAnnotatedWithShouldNotHaveModifiers(annotationType.getName(), notRequiredModifiers,
        defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that methods annotated with a given annotation type
   * must not have all the specified modifiers, using a custom configuration.
   *
   * @param annotationType the annotation type to check
   * @param notRequiredModifiers the not required {@link JavaModifier}s
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsAnnotatedWithShouldNotHaveModifiers(
      Class<? extends Annotation> annotationType,
      Collection<JavaModifier> notRequiredModifiers, Configuration configuration) {
    return methodsAnnotatedWithShouldNotHaveModifiers(annotationType.getName(), notRequiredModifiers,
        configuration);
  }

  /**
   * Adds a rule enforcing that methods annotated with an annotation whose name matches
   * the given type name must have all the specified modifiers.
   *
   * @param annotationType the annotation type name to check
   * @param notRequiredModifiers the required {@link JavaModifier}s
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsAnnotatedWithShouldNotHaveModifiers(String annotationType,
      Collection<JavaModifier> notRequiredModifiers) {
    return methodsAnnotatedWithShouldNotHaveModifiers(annotationType, notRequiredModifiers,
        defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that methods annotated with an annotation whose name matches
   * the given type name must not have all the specified modifiers, using a custom configuration.
   *
   * @param annotationType the annotation type name to check
   * @param notRequiredModifiers the not required {@link JavaModifier}s
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsAnnotatedWithShouldNotHaveModifiers(String annotationType,
      Collection<JavaModifier> notRequiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().areAnnotatedWith(annotationType)
            .should(notHasMethodModifiers(notRequiredModifiers))
            .as("Methods annotated with %s should not have all of this modifiers %s".formatted(
                annotationType,
                notRequiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that methods whose names match a regex
   * must have the specified modifiers.
   *
   * @param regex the regex for method names
   * @param requiredModifiers the required modifiers
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldHaveModifiers(String regex,
      Collection<JavaModifier> requiredModifiers) {
    return methodsShouldHaveModifiers(regex, requiredModifiers, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that methods whose names match a regex
   * must have the specified modifiers, using a custom configuration.
   *
   * @param regex the regex for method names
   * @param requiredModifiers the required modifiers
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldHaveModifiers(String regex,
      Collection<JavaModifier> requiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().haveNameMatching(regex)
            .should(hasMethodsModifiers(requiredModifiers))
            .as("Methods have name matching %s should have all of this modifiers %s".formatted(
                regex,
                requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that methods whose names match a regex
   * must not have the specified modifiers.
   *
   * @param regex the regex for method names
   * @param notRequiredModifiers the not required modifiers
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldNotHaveModifiers(String regex,
      Collection<JavaModifier> notRequiredModifiers) {
    return methodsShouldNotHaveModifiers(regex, notRequiredModifiers, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that methods whose names match a regex
   * must not have the specified modifiers, using a custom configuration.
   *
   * @param regex the regex for method names
   * @param notRequiredModifiers the not required modifiers
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldNotHaveModifiers(String regex,
      Collection<JavaModifier> notRequiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().haveNameMatching(regex)
            .should(notHasMethodModifiers(notRequiredModifiers))
            .as("Methods have name matching %s should not have all of this modifiers %s".formatted(
                regex,
                notRequiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that all methods declared in a specific class (by regex)
   * must have the specified modifiers.
   *
   * @param regex regex for class name
   * @param requiredModifiers required modifiers for methods
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldHaveModifiersForClass(String regex,
      Collection<JavaModifier> requiredModifiers) {
    return methodsShouldHaveModifiersForClass(regex, requiredModifiers, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that all methods declared in a specific class (by regex)
   * must have the specified modifiers, using a custom configuration.
   *
   * @param regex regex for class name
   * @param requiredModifiers required modifiers for methods
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldHaveModifiersForClass(String regex,
      Collection<JavaModifier> requiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().areDeclaredInClassesThat().haveNameMatching(regex)
            .should(hasMethodsModifiers(requiredModifiers))
            .as("Methods in class %s should have all of this modifiers %s".formatted(
                regex,
                requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that all methods declared in a specific class (by regex)
   * must not have the specified modifiers.
   *
   * @param regex regex for class name
   * @param notRequiredModifiers not required modifiers for methods
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldNotHaveModifiersForClass(String regex,
      Collection<JavaModifier> notRequiredModifiers) {
    return methodsShouldNotHaveModifiersForClass(regex, notRequiredModifiers, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that all methods declared in a specific class (by regex)
   * must not have the specified modifiers, using a custom configuration.
   *
   * @param regex regex for class name
   * @param notRequiredModifiers not required modifiers for methods
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldNotHaveModifiersForClass(String regex,
      Collection<JavaModifier> notRequiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().areDeclaredInClassesThat().haveNameMatching(regex)
            .should(notHasMethodModifiers(notRequiredModifiers))
            .as("Methods in class %s should not have all of this modifiers %s".formatted(
                regex,
                notRequiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule ensuring methods do not exceed a specific number of parameters.
   *
   * @param maxMethodParameters maximum number of allowed parameters
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldNotExceedMaxParameters(int maxMethodParameters) {
    return methodsShouldNotExceedMaxParameters(maxMethodParameters, defaultConfiguration());
  }

  /**
   * Adds a rule ensuring methods do not exceed a specific number of parameters,
   * using a custom configuration.
   *
   * @param maxMethodParameters maximum number of allowed parameters
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer methodsShouldNotExceedMaxParameters(int maxMethodParameters, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .should(notExceedMaxParameters(maxMethodParameters))
        .as("Methods should not have more than %d parameters".formatted(maxMethodParameters)), configuration));
  }

  /**
   * Adds a rule prohibiting usage of deprecated APIs.
   *
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer noUsageOfDeprecatedAPIs() {
    return noUsageOfDeprecatedAPIs(defaultConfiguration());
  }

  /**
   * Adds a rule prohibiting usage of deprecated APIs, using a custom configuration.
   *
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer noUsageOfDeprecatedAPIs(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(notUseDeprecatedAPIs())
        .as("Classes should not use deprecated APIs"), configuration));
  }

  /**
   * Adds a rule enforcing that all classes reside in a specific package.
   *
   * @param packageIdentifier the expected package pattern
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldResideInPackage(String packageIdentifier) {
    return classesShouldResideInPackage(packageIdentifier, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that all classes reside in a specific package,
   * using a custom configuration.
   *
   * @param packageIdentifier the expected package pattern
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldResideInPackage(String packageIdentifier,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .should().resideInAPackage(packageIdentifier)
            .as("Package names should match %s".formatted(packageIdentifier)),
        configuration));
  }

  /**
   * Adds a rule enforcing that classes with names matching a regex
   * must reside in a specific package.
   *
   * @param regex the regex for class names
   * @param packageIdentifier the required package
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldResideInPackage(String regex, String packageIdentifier) {
    return classesShouldResideInPackage(regex, packageIdentifier, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes with names matching a regex
   * must reside in a specific package, using a custom configuration.
   *
   * @param regex the regex for class names
   * @param packageIdentifier the required package
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldResideInPackage(String regex, String packageIdentifier,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveNameMatching(regex)
        .should().resideInAPackage(packageIdentifier)
        .as("Classes have name matching %s should reside in package %s".formatted(
            regex, packageIdentifier)), configuration));
  }

  /**
   * Adds a rule enforcing that classes with names matching a regex
   * must reside outside a specific package.
   *
   * @param regex the regex for class names
   * @param packageIdentifier the forbidden package
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldResideOutsidePackage(String regex, String packageIdentifier) {
    return classesShouldResideOutsidePackage(regex, packageIdentifier, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes with names matching a regex
   * must reside outside a specific package, using a custom configuration.
   *
   * @param regex the regex for class names
   * @param packageIdentifier the forbidden package
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldResideOutsidePackage(String regex, String packageIdentifier,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveNameMatching(regex)
        .should().resideOutsideOfPackage(packageIdentifier)
        .as("Classes have name matching %s should reside outside package %s".formatted(
            regex, packageIdentifier)), configuration));
  }

  /**
   * Adds a rule enforcing that classes annotated with a specific annotation
   * must also be annotated with all required annotations.
   *
   * @param annotationType the base annotation type
   * @param requiredAnnotationTypes the required annotation types
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldBeAnnotatedWithAll(Class<? extends Annotation> annotationType,
      Collection<Class<? extends Annotation>> requiredAnnotationTypes) {
    return classesShouldBeAnnotatedWithAll(annotationType.getName(),
        requiredAnnotationTypes.stream().map(Class::getName).toList(), defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes annotated with a specific annotation
   * must also be annotated with all required annotations, using a custom configuration.
   *
   * @param annotationType the base annotation type
   * @param requiredAnnotationTypes the required annotation types
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldBeAnnotatedWithAll(Class<? extends Annotation> annotationType,
      Collection<Class<? extends Annotation>> requiredAnnotationTypes,
      Configuration configuration) {
    return classesShouldBeAnnotatedWithAll(annotationType.getName(),
        requiredAnnotationTypes.stream().map(Class::getName).toList(), configuration);
  }

  /**
   * Adds a rule enforcing that classes annotated with a specific annotation (by name)
   * must also be annotated with all required annotations.
   *
   * @param annotationType the base annotation type name
   * @param requiredAnnotationTypes the required annotation type names
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldBeAnnotatedWithAll(String annotationType,
      Collection<String> requiredAnnotationTypes) {
    return classesShouldBeAnnotatedWithAll(annotationType, requiredAnnotationTypes,
        defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes annotated with a specific annotation (by name)
   * must also be annotated with all required annotations, using a custom configuration.
   *
   * @param annotationType the base annotation type name
   * @param requiredAnnotationTypes the required annotation type names
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldBeAnnotatedWithAll(String annotationType,
      Collection<String> requiredAnnotationTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areMetaAnnotatedWith(annotationType)
            .should(be(annotatedWithAll(requiredAnnotationTypes, true)))
            .as("Classes annotated with %s should be annotated with %s".formatted(
                annotationType, String.join(", ", requiredAnnotationTypes))),
        configuration));
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * must be annotated with a specific annotation.
   *
   * @param regex the regex for class names
   * @param annotationType the required annotation
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType) {
    return classesShouldBeAnnotatedWith(regex, annotationType.getName(), defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * must be annotated with a specific annotation, using a custom configuration.
   *
   * @param regex the regex for class names
   * @param annotationType the required annotation
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType, Configuration configuration) {
    return classesShouldBeAnnotatedWith(regex, annotationType.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * must be annotated with a specific annotation by name.
   *
   * @param regex the regex for class names
   * @param annotationType the required annotation type name
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldBeAnnotatedWith(String regex, String annotationType) {
    return classesShouldBeAnnotatedWith(regex, annotationType, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * must be annotated with a specific annotation by name, using a custom configuration.
   *
   * @param regex the regex for class names
   * @param annotationType the required annotation type name
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldBeAnnotatedWith(String regex, String annotationType,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveNameMatching(regex)
        .should().beMetaAnnotatedWith(annotationType)
        .as("Classes have name matching %s should be annotated with %s".formatted(regex,
            annotationType)), configuration));
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * must not be annotated with a specific annotation.
   *
   * @param regex the regex for class names
   * @param annotationType the forbidden annotation
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldNotBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType) {
    return classesShouldNotBeAnnotatedWith(regex, annotationType.getName(), defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * must not be annotated with a specific annotation, using a custom configuration.
   *
   * @param regex the regex for class names
   * @param annotationType the forbidden annotation
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldNotBeAnnotatedWith(String regex,
      Class<? extends Annotation> annotationType, Configuration configuration) {
    return classesShouldNotBeAnnotatedWith(regex, annotationType.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * must not be annotated with a specific annotation by name.
   *
   * @param regex the regex for class names
   * @param annotationType the forbidden annotation type name
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldNotBeAnnotatedWith(String regex, String annotationType) {
    return classesShouldNotBeAnnotatedWith(regex, annotationType, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * must not be annotated with a specific annotation by name, using a custom configuration.
   *
   * @param regex the regex for class names
   * @param annotationType the forbidden annotation type name
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldNotBeAnnotatedWith(String regex, String annotationType,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveNameMatching(regex)
        .should().notBeMetaAnnotatedWith(annotationType)
        .as("Classes have name matching %s should not be annotated with %s".formatted(regex,
            annotationType)), configuration));
  }

  /**
   * Adds a rule enforcing that classes annotated with a specific annotation
   * must not also be annotated with another conflicting annotation.
   *
   * @param annotationType the base annotation
   * @param notAnnotationType the disallowed annotation
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesAnnotatedWithShouldNotBeAnnotatedWith(Class<? extends Annotation> annotationType,
      Class<? extends Annotation> notAnnotationType) {
    return classesAnnotatedWithShouldNotBeAnnotatedWith(annotationType.getName(), notAnnotationType.getName(), defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes annotated with a specific annotation
   * must not also be annotated with another conflicting annotation, using a custom configuration.
   *
   * @param annotationType the base annotation
   * @param notAnnotationType the disallowed annotation
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesAnnotatedWithShouldNotBeAnnotatedWith(Class<? extends Annotation> annotationType,
      Class<? extends Annotation> notAnnotationType, Configuration configuration) {
    return classesAnnotatedWithShouldNotBeAnnotatedWith(annotationType.getName(), notAnnotationType.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that classes annotated with a specific annotation (by name)
   * must not also be annotated with another annotation.
   *
   * @param annotationType the base annotation type name
   * @param notAnnotationType the disallowed annotation type name
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesAnnotatedWithShouldNotBeAnnotatedWith(String annotationType, String notAnnotationType) {
    return classesAnnotatedWithShouldNotBeAnnotatedWith(annotationType, notAnnotationType, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes annotated with a specific annotation (by name)
   * must not also be annotated with another annotation, using a custom configuration.
   *
   * @param annotationType the base annotation type name
   * @param notAnnotationType the disallowed annotation type name
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesAnnotatedWithShouldNotBeAnnotatedWith(String annotationType, String notAnnotationType,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().areAnnotatedWith(annotationType)
        .should().notBeMetaAnnotatedWith(notAnnotationType)
        .as("Classes annotated with %s should not be annotated with %s".formatted(annotationType,
            notAnnotationType)), configuration));
  }

  /**
   * Adds a rule enforcing that classes annotated with a specific annotation
   * must reside within a specific package.
   *
   * @param annotationType the annotation the classes must have
   * @param packageIdentifier the package they must reside in
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesAnnotatedWithShouldResideInPackage(
      Class<? extends Annotation> annotationType, String packageIdentifier) {
    return classesAnnotatedWithShouldResideInPackage(annotationType.getName(), packageIdentifier,
        defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes annotated with a specific annotation
   * must reside within a specific package, using a custom configuration.
   *
   * @param annotationType the annotation the classes must have
   * @param packageIdentifier the package they must reside in
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesAnnotatedWithShouldResideInPackage(
      Class<? extends Annotation> annotationType, String packageIdentifier,
      Configuration configuration) {
    return classesAnnotatedWithShouldResideInPackage(annotationType.getName(), packageIdentifier,
        configuration);
  }

  /**
   * Adds a rule enforcing that classes annotated with a specific annotation (by name)
   * must reside within a specific package.
   *
   * @param annotationType the annotation type name
   * @param packageIdentifier the required package
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesAnnotatedWithShouldResideInPackage(
      String annotationType, String packageIdentifier) {
    return classesAnnotatedWithShouldResideInPackage(annotationType, packageIdentifier,
        defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes annotated with a specific annotation (by name)
   * must reside within a specific package, using a custom configuration.
   *
   * @param annotationType the annotation type name
   * @param packageIdentifier the required package
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesAnnotatedWithShouldResideInPackage(
      String annotationType, String packageIdentifier, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().areMetaAnnotatedWith(annotationType)
        .should().resideInAPackage(packageIdentifier)
        .as("Classes annotated with %s should reside in package %s".formatted(
            annotationType, packageIdentifier)), configuration));
  }

  /**
   * Adds a rule enforcing that all classes implement both {@code hashCode()} and {@code equals()}.
   *
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldImplementHashCodeAndEquals() {
    return classesShouldImplementHashCodeAndEquals(defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that all classes implement both {@code hashCode()} and {@code equals()},
   * using a custom configuration.
   *
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldImplementHashCodeAndEquals(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(implementHashCodeAndEquals())
        .as("Classes should implement hashCode and equals"), configuration));
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * should be assignable to a specific class type.
   *
   * @param regex the regex for class names
   * @param clazz the target class type
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldBeAssignableTo(String regex, Class<?> clazz) {
    return classesShouldBeAssignableTo(regex, clazz.getName(), defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * should be assignable to a specific class type, using a custom configuration.
   *
   * @param regex the regex for class names
   * @param clazz the target class type
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldBeAssignableTo(String regex, Class<?> clazz,
      Configuration configuration) {
    return classesShouldBeAssignableTo(regex, clazz.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * should be assignable to a specific type by name.
   *
   * @param regex the regex for class names
   * @param typeName the target type name
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldBeAssignableTo(String regex, String typeName) {
    return classesShouldBeAssignableTo(regex, typeName, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * should be assignable to a specific type by name, using a custom configuration.
   *
   * @param regex the regex for class names
   * @param typeName the target type name
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldBeAssignableTo(String regex, String typeName,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveSimpleNameEndingWith(regex)
        .should().beAssignableTo(typeName)
        .as("Classes have name matching %s should be assignable to %s".formatted(
            regex, typeName)), configuration));
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * should implement a specific interface type.
   *
   * @param regex the regex for class names
   * @param clazz the interface class
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldImplement(String regex, Class<?> clazz) {
    return classesShouldImplement(regex, clazz.getName(), defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * should implement a specific interface type, using a custom configuration.
   *
   * @param regex the regex for class names
   * @param clazz the interface class
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldImplement(String regex, Class<?> clazz,
      Configuration configuration) {
    return classesShouldImplement(regex, clazz.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * should implement a specific interface type by name.
   *
   * @param regex the regex for class names
   * @param typeName the interface type name
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldImplement(String regex, String typeName) {
    return classesShouldImplement(regex, typeName, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * should implement a specific interface type by name, using a custom configuration.
   *
   * @param regex the regex for class names
   * @param typeName the interface type name
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldImplement(String regex, String typeName,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveSimpleNameEndingWith(regex)
        .should().implement(typeName)
        .as("Classes have name matching %s should implement %s".formatted(
            regex, typeName)), configuration));
  }
  /**
   * Adds a rule enforcing that classes whose names match a regex
   * must have all specified modifiers.
   *
   * @param regex the regex for class names
   * @param requiredModifiers the required modifiers
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldHaveModifiers(String regex,
      Collection<JavaModifier> requiredModifiers) {
    return classesShouldHaveModifiers(regex, requiredModifiers, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that classes whose names match a regex
   * must have all specified modifiers, using a custom configuration.
   *
   * @param regex the regex for class names
   * @param requiredModifiers the required modifiers
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer classesShouldHaveModifiers(String regex,
      Collection<JavaModifier> requiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(hasClassModifiers(requiredModifiers))
            .as("Classes have name matching %s should have all of this modifiers %s".formatted(
                regex,
                requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that fields should not be {@code public}
   * unless they are also {@code static}.
   *
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer fieldsShouldNotBePublic() {
    return fieldsShouldNotBePublic(defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that fields should not be {@code public}
   * unless they are also {@code static}, using a custom configuration.
   *
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer fieldsShouldNotBePublic(Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .should(notBePublicUnlessStatic())
        .as("Fields should not be public unless they are static"), configuration));
  }

  /**
   * Adds a rule enforcing that fields whose names match a regex
   * must have all specified modifiers.
   *
   * @param regex the regex for field names
   * @param requiredModifiers the required modifiers
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer fieldsShouldHaveModifiers(String regex,
      Collection<JavaModifier> requiredModifiers) {
    return fieldsShouldHaveModifiers(regex, requiredModifiers, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that fields whose names match a regex
   * must have all specified modifiers, using a custom configuration.
   *
   * @param regex the regex for field names
   * @param requiredModifiers the required modifiers
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
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

  /**
   * Adds a rule prohibiting usage of a specific class.
   *
   * @param clazz the class type that must not be used
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer noUsageOf(Class<?> clazz) {
    return noUsageOf(clazz.getName(), null, defaultConfiguration());
  }

  /**
   * Adds a rule prohibiting usage of a specific class
   * within a given package.
   *
   * @param clazz the class type that must not be used
   * @param packageIdentifier the package where it is disallowed
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer noUsageOf(Class<?> clazz, String packageIdentifier) {
    return noUsageOf(clazz.getName(), packageIdentifier, defaultConfiguration());
  }

  /**
   * Adds a rule prohibiting usage of a specific class
   * within a given package, using a custom configuration.
   *
   * @param clazz the class type that must not be used
   * @param packageIdentifier the package where it is disallowed
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer noUsageOf(Class<?> clazz, String packageIdentifier,
      Configuration configuration) {
    return noUsageOf(clazz.getName(), packageIdentifier, configuration);
  }

  /**
   * Adds a rule prohibiting usage of a specific class, with a custom configuration.
   *
   * @param clazz the class type that must not be used
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer noUsageOf(Class<?> clazz, Configuration configuration) {
    return noUsageOf(clazz.getName(), null, configuration);
  }

  /**
   * Adds a rule prohibiting usage of a specific type by name.
   *
   * @param typeName the fully qualified class name that must not be used
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer noUsageOf(String typeName) {
    return noUsageOf(typeName, null, defaultConfiguration());
  }

  /**
   * Adds a rule prohibiting usage of a specific type by name
   * within a given package.
   *
   * @param typeName the fully qualified class name that must not be used
   * @param packageIdentifier the package where it is disallowed
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer noUsageOf(String typeName, String packageIdentifier) {
    return noUsageOf(typeName, packageIdentifier, defaultConfiguration());
  }

  /**
   * Adds a rule prohibiting usage of a specific type by name,
   * using a custom configuration.
   *
   * @param typeName the fully qualified class name that must not be used
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer noUsageOf(String typeName, Configuration configuration) {
    return noUsageOf(typeName, null, configuration);
  }

  /**
   * Adds a rule prohibiting usage of a specific type by name
   * within a given package, using a custom configuration.
   *
   * @param typeName the fully qualified class name that must not be used
   * @param packageIdentifier the package where it is disallowed
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer noUsageOf(String typeName, @Nullable String packageIdentifier,
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

  /**
   * Adds a rule prohibiting the usage of {@code System.out} and {@code System.err}.
   *
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer noUsageOfSystemOutOrErr() {
    return noUsageOfSystemOutOrErr(defaultConfiguration());
  }

  /**
   * Adds a rule prohibiting the usage of {@code System.out} and {@code System.err},
   * using a custom configuration.
   *
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer noUsageOfSystemOutOrErr(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(notUseSystemOutOrErr())
        .as("Classes should not use System.out or System.err"), configuration));
  }

  /**
   * Adds a rule enforcing that {@code final} classes must not have any protected members.
   *
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer finalClassesShouldNotHaveProtectedMembers() {
    return finalClassesShouldNotHaveProtectedMembers(defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that {@code final} classes must not have any protected members,
   * using a custom configuration.
   *
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer finalClassesShouldNotHaveProtectedMembers(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(areFinal())
        .should(notHaveProtectedMembers())
        .as("Final classes should not have protected members"), configuration));
  }

  /**
   * Adds a rule enforcing that {@code serialVersionUID} fields
   * must be {@code static final long}.
   *
   * @return this {@link JavaConfigurer} for fluent chaining
   */
  public JavaConfigurer serialVersionUIDFieldsShouldBeStaticFinalLong() {
    return serialVersionUIDFieldsShouldBeStaticFinalLong(defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that {@code serialVersionUID} fields
   * must be {@code static final long}, using a custom configuration.
   *
   * @param configuration the configuration to use
   * @return this {@link JavaConfigurer} for fluent chaining
   */
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