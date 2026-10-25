package com.enofex.taikai.java;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.internal.ArchConditions.hasClassModifiers;
import static com.enofex.taikai.internal.ArchConditions.hasFieldModifiers;
import static com.enofex.taikai.internal.ArchConditions.hasMethodsModifiers;
import static com.enofex.taikai.internal.ArchConditions.haveFieldMatching;
import static com.enofex.taikai.internal.ArchConditions.notBePublicUnlessStatic;
import static com.enofex.taikai.internal.ArchConditions.notHasClassModifiers;
import static com.enofex.taikai.internal.ArchConditions.notHasFieldModifiers;
import static com.enofex.taikai.internal.ArchConditions.notHasMethodModifiers;
import static com.enofex.taikai.internal.ArchConditions.notHaveFieldTypesMatching;
import static com.enofex.taikai.internal.ArchConditions.notReturnTypesMatching;
import static com.enofex.taikai.internal.ArchConditions.returnTypesMatching;
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
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Collectors;

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
public final class JavaConfigurer extends AbstractConfigurer implements DisableableConfigurer {

  public JavaConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  /**
   * Configures import rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link ImportsConfigurer}
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer imports(Customizer<ImportsConfigurer> customizer) {
    return customizer(customizer, () -> new ImportsConfigurer(configurerContext()));
  }

  /**
   * Configures naming rules using the provided {@link Customizer}.
   *
   * @param customizer the customizer for {@link NamingConfigurer}
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer naming(Customizer<NamingConfigurer> customizer) {
    return customizer(customizer, () -> new NamingConfigurer(configurerContext()));
  }

  /**
   * Prevents methods matching the given criteria from returning matching types, including
   * generic arguments, bounds, and array components. All method visibilities are checked.
   * Use {@link com.enofex.taikai.ClassCriteria} or ArchUnit predicates to select the types.
   *
   * @param methods the methods to check, selected with
   *     {@link com.enofex.taikai.MethodCriteria} or an ArchUnit predicate
   * @param forbiddenTypes the forbidden return types
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotReturn(
      DescribedPredicate<? super JavaMethod> methods,
      DescribedPredicate<? super JavaClass> forbiddenTypes) {
    return methodsShouldNotReturn(methods, forbiddenTypes, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotReturn(DescribedPredicate, DescribedPredicate)}, with a
   * custom configuration.
   *
   * @param methods the methods to check
   * @param forbiddenTypes the forbidden return types
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotReturn(
      DescribedPredicate<? super JavaMethod> methods,
      DescribedPredicate<? super JavaClass> forbiddenTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that(methods)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(notReturnTypesMatching(forbiddenTypes))
        .as("Methods %s should not return types %s".formatted(
            methods.getDescription(), forbiddenTypes.getDescription())), configuration));
  }

  /**
   * Prevents methods declared in matching classes from returning matching types, including
   * generic arguments, bounds, and array components. All method visibilities are checked.
   * Use {@link com.enofex.taikai.ClassCriteria} or ArchUnit predicates to select the types.
   *
   * @param classes the classes whose declared methods are checked
   * @param forbiddenTypes the forbidden return types
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotReturnForClass(
      DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> forbiddenTypes) {
    return methodsShouldNotReturnForClass(classes, forbiddenTypes, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotReturnForClass(DescribedPredicate, DescribedPredicate)}, with a
   * custom configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param forbiddenTypes the forbidden return types
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotReturnForClass(
      DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> forbiddenTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().areDeclaredInClassesThat(classes)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(notReturnTypesMatching(forbiddenTypes))
        .as("Methods declared in classes %s should not return types %s".formatted(
            classes.getDescription(), forbiddenTypes.getDescription())), configuration));
  }

  /**
   * Requires each method declared in matching classes to have at least one matching type in
   * its return signature, including generic arguments, bounds, and array components.
   * All method visibilities are checked. Use {@link com.enofex.taikai.ClassCriteria} or ArchUnit
   * predicates to select the types.
   *
   * @param methods the methods to check, selected with
   *     {@link com.enofex.taikai.MethodCriteria} or an ArchUnit predicate
   * @param requiredTypes the required return types
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldReturn(
      DescribedPredicate<? super JavaMethod> methods,
      DescribedPredicate<? super JavaClass> requiredTypes) {
    return methodsShouldReturn(methods, requiredTypes, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldReturn(DescribedPredicate, DescribedPredicate)}, with a custom
   * configuration.
   *
   * @param methods the methods to check
   * @param requiredTypes the required return types
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldReturn(
      DescribedPredicate<? super JavaMethod> methods,
      DescribedPredicate<? super JavaClass> requiredTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that(methods)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(returnTypesMatching(requiredTypes))
        .as("Methods %s should return types %s".formatted(
            methods.getDescription(), requiredTypes.getDescription())), configuration));
  }

  /**
   * Requires each method declared in matching classes to have at least one matching type in
   * its return signature, including generic arguments, bounds, and array components.
   * All method visibilities are checked. Use {@link com.enofex.taikai.ClassCriteria} or ArchUnit
   * predicates to select the types.
   *
   * @param classes the classes whose declared methods are checked
   * @param requiredTypes the required return types
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldReturnForClass(
      DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> requiredTypes) {
    return methodsShouldReturnForClass(classes, requiredTypes, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldReturnForClass(DescribedPredicate, DescribedPredicate)}, with a custom
   * configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param requiredTypes the required return types
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldReturnForClass(
      DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> requiredTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().areDeclaredInClassesThat(classes)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(returnTypesMatching(requiredTypes))
        .as("Methods declared in classes %s should return types %s".formatted(
            classes.getDescription(), requiredTypes.getDescription())), configuration));
  }

  /**
   * Adds a rule enforcing that utility classes must be {@code final}
   * and have a private constructor.
   *
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer utilityClassesShouldBeFinalAndHavePrivateConstructor() {
    return utilityClassesShouldBeFinalAndHavePrivateConstructor(defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that utility classes must be {@code final}
   * and have a private constructor, using a custom {@link Configuration}.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
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
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer methodsShouldNotDeclareGenericExceptions() {
    return methodsShouldNotDeclareGenericExceptions(defaultConfiguration());
  }

  /**
   * Adds a rule preventing methods from declaring {@code Exception}
   * or {@code RuntimeException}, using a custom {@link Configuration}.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer methodsShouldNotDeclareGenericExceptions(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().notDeclareThrowableOfType(Exception.class)
         .andShould().notDeclareThrowableOfType(RuntimeException.class)
        .as("Methods should not declare generic Exception or RuntimeException"), configuration));
  }

  /**
   * Adds a rule enforcing that methods matching the given criteria must not declare the given
   * exception. Synthetic methods generated by the compiler are ignored.
   *
   * @param methods the methods to check, selected with {@link com.enofex.taikai.MethodCriteria}, for
   *     example {@code MethodCriteria.nameMatching("find.*")}, or an ArchUnit predicate
   * @param clazz the exception type that must not be declared
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotDeclareException(
      DescribedPredicate<? super JavaMethod> methods, Class<? extends Throwable> clazz) {
    return methodsShouldNotDeclareException(methods, clazz, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotDeclareException(DescribedPredicate, Class)}, with a custom
   * configuration.
   *
   * @param methods the methods to check
   * @param clazz the exception type that must not be declared
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotDeclareException(
      DescribedPredicate<? super JavaMethod> methods, Class<? extends Throwable> clazz,
      Configuration configuration) {
    return methodsShouldNotDeclareException(methods, clazz.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that methods matching the given criteria must not declare the given
   * exception. Synthetic methods generated by the compiler are ignored.
   *
   * @param methods the methods to check, selected with {@link com.enofex.taikai.MethodCriteria}, for
   *     example {@code MethodCriteria.nameMatching("find.*")}, or an ArchUnit predicate
   * @param typeName the fully qualified name of the exception type that must not be declared
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotDeclareException(
      DescribedPredicate<? super JavaMethod> methods, String typeName) {
    return methodsShouldNotDeclareException(methods, typeName, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotDeclareException(DescribedPredicate, String)}, with a custom
   * configuration.
   *
   * @param methods the methods to check
   * @param typeName the fully qualified name of the exception type that must not be declared
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotDeclareException(
      DescribedPredicate<? super JavaMethod> methods, String typeName,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(methods)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().notDeclareThrowableOfType(typeName)
            .as("Methods %s should not declare %s".formatted(
                methods.getDescription(), typeName)),
        configuration));
  }

  /**
   * Adds a rule enforcing that methods matching the given criteria must not declare any exception
   * matching other criteria. Synthetic methods generated by the compiler are ignored.
   *
   * @param methods the methods to check, selected with {@link com.enofex.taikai.MethodCriteria}, for
   *     example {@code MethodCriteria.nameMatching("find.*")}, or an ArchUnit predicate
   * @param forbiddenTypes the exception types that must not be declared, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotDeclareException(
      DescribedPredicate<? super JavaMethod> methods,
      DescribedPredicate<? super JavaClass> forbiddenTypes) {
    return methodsShouldNotDeclareException(methods, forbiddenTypes, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotDeclareException(DescribedPredicate, DescribedPredicate)}, with a
   * custom configuration.
   *
   * @param methods the methods to check
   * @param forbiddenTypes the exception types that must not be declared, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotDeclareException(
      DescribedPredicate<? super JavaMethod> methods,
      DescribedPredicate<? super JavaClass> forbiddenTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(methods)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().notDeclareThrowableOfType(forbiddenTypes)
            .as("Methods %s should not declare exceptions %s".formatted(
                methods.getDescription(), forbiddenTypes.getDescription())),
        configuration));
  }

  /**
   * Adds a rule preventing all methods declared in classes matching the given criteria
   * from declaring a specific exception type. Synthetic methods generated by the compiler,
   * such as bridge methods with erased {@code throws} clauses, are ignored.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param clazz the exception type
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotDeclareExceptionForClass(DescribedPredicate<? super JavaClass> classes,
      Class<? extends Throwable> clazz) {
    return methodsShouldNotDeclareExceptionForClass(classes, clazz.getName(),
        defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotDeclareExceptionForClass(DescribedPredicate, Class)}, with a
   * custom configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param clazz the exception type
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotDeclareExceptionForClass(DescribedPredicate<? super JavaClass> classes,
      Class<? extends Throwable> clazz, Configuration configuration) {
    return methodsShouldNotDeclareExceptionForClass(classes, clazz.getName(), configuration);
  }

  /**
   * Adds a rule preventing all methods declared in classes matching the given criteria
   * from declaring a specific exception type by name. Synthetic methods generated by the
   * compiler are ignored.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param typeName the exception type name
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotDeclareExceptionForClass(DescribedPredicate<? super JavaClass> classes,
      String typeName) {
    return methodsShouldNotDeclareExceptionForClass(classes, typeName, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotDeclareExceptionForClass(DescribedPredicate, String)}, with a
   * custom configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param typeName the exception type name
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotDeclareExceptionForClass(DescribedPredicate<? super JavaClass> classes,
      String typeName, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().areDeclaredInClassesThat(classes)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().notDeclareThrowableOfType(typeName)
            .as("Methods declared in classes %s should not declare %s".formatted(
                classes.getDescription(), typeName)),
        configuration));
  }

  /**
   * Adds a rule preventing all methods declared in classes matching the given criteria
   * from declaring any exception type matching other criteria. Synthetic methods generated by
   * the compiler are ignored.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param forbiddenTypes the forbidden exception types
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotDeclareExceptionForClass(DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> forbiddenTypes) {
    return methodsShouldNotDeclareExceptionForClass(classes, forbiddenTypes,
        defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotDeclareExceptionForClass(DescribedPredicate,
   * DescribedPredicate)}, with a custom configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param forbiddenTypes the forbidden exception types
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotDeclareExceptionForClass(DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> forbiddenTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().areDeclaredInClassesThat(classes)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().notDeclareThrowableOfType(forbiddenTypes)
            .as("Methods declared in classes %s should not declare exceptions %s".formatted(
                classes.getDescription(), forbiddenTypes.getDescription())),
        configuration));
  }

  /**
   * Adds a rule enforcing that methods matching the given criteria must be annotated with the given
   * annotation. Synthetic methods generated by the compiler are ignored.
   *
   * @param methods the methods to check, selected with {@link com.enofex.taikai.MethodCriteria}, for
   *     example {@code MethodCriteria.nameMatching("find.*")}, or an ArchUnit predicate
   * @param annotationType the annotation that must be present
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldBeAnnotatedWith(
      DescribedPredicate<? super JavaMethod> methods, Class<? extends Annotation> annotationType) {
    return methodsShouldBeAnnotatedWith(methods, annotationType, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldBeAnnotatedWith(DescribedPredicate, Class)}, with a custom
   * configuration.
   *
   * @param methods the methods to check
   * @param annotationType the annotation that must be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldBeAnnotatedWith(
      DescribedPredicate<? super JavaMethod> methods, Class<? extends Annotation> annotationType,
      Configuration configuration) {
    return methodsShouldBeAnnotatedWith(methods, annotationType.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that methods matching the given criteria must be annotated with the given
   * annotation. Synthetic methods generated by the compiler are ignored.
   *
   * @param methods the methods to check, selected with {@link com.enofex.taikai.MethodCriteria}, for
   *     example {@code MethodCriteria.nameMatching("find.*")}, or an ArchUnit predicate
   * @param annotationType the annotation that must be present
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldBeAnnotatedWith(
      DescribedPredicate<? super JavaMethod> methods, String annotationType) {
    return methodsShouldBeAnnotatedWith(methods, annotationType, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldBeAnnotatedWith(DescribedPredicate, String)}, with a custom
   * configuration.
   *
   * @param methods the methods to check
   * @param annotationType the annotation that must be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldBeAnnotatedWith(
      DescribedPredicate<? super JavaMethod> methods, String annotationType,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(methods)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should().beMetaAnnotatedWith(annotationType)
            .as("Methods %s should be annotated with %s".formatted(
                methods.getDescription(), annotationType)),
        configuration));
  }

  /**
   * Adds a rule enforcing that all methods declared in classes matching the given criteria
   * must be annotated with a specific annotation. All method visibilities are checked;
   * synthetic methods generated by the compiler are ignored.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param annotationType the required annotation class
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldBeAnnotatedWithForClass(
      DescribedPredicate<? super JavaClass> classes,
      Class<? extends Annotation> annotationType) {
    return methodsShouldBeAnnotatedWithForClass(classes, annotationType.getName(),
        defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldBeAnnotatedWithForClass(DescribedPredicate, Class)}, with a custom
   * configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param annotationType the required annotation class
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldBeAnnotatedWithForClass(
      DescribedPredicate<? super JavaClass> classes,
      Class<? extends Annotation> annotationType, Configuration configuration) {
    return methodsShouldBeAnnotatedWithForClass(classes, annotationType.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that all methods declared in classes matching the given criteria
   * must be annotated with a specific annotation by name. All method visibilities are checked;
   * synthetic methods generated by the compiler are ignored.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param annotationType the required annotation type name
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldBeAnnotatedWithForClass(
      DescribedPredicate<? super JavaClass> classes,
      String annotationType) {
    return methodsShouldBeAnnotatedWithForClass(classes, annotationType, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldBeAnnotatedWithForClass(DescribedPredicate, String)}, with a custom
   * configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param annotationType the required annotation type name
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldBeAnnotatedWithForClass(
      DescribedPredicate<? super JavaClass> classes,
      String annotationType, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().areDeclaredInClassesThat(classes)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().beMetaAnnotatedWith(annotationType)
        .as("Methods declared in classes %s should be annotated with %s".formatted(
            classes.getDescription(), annotationType)), configuration));
  }

  /**
   * Adds a rule enforcing that methods matching the given criteria
   * must be annotated with all required annotations.
   *
   * @param methods the methods to check, selected with {@link com.enofex.taikai.MethodCriteria}
   *     or an ArchUnit predicate
   * @param requiredAnnotationTypes the required annotation type names
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldBeAnnotatedWithAll(
      DescribedPredicate<? super JavaMethod> methods, Collection<String> requiredAnnotationTypes) {
    return methodsShouldBeAnnotatedWithAll(methods, requiredAnnotationTypes,
        defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldBeAnnotatedWithAll(DescribedPredicate, Collection)}, with a custom
   * configuration.
   *
   * @param methods the methods to check
   * @param requiredAnnotationTypes the required annotation type names
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldBeAnnotatedWithAll(
      DescribedPredicate<? super JavaMethod> methods, Collection<String> requiredAnnotationTypes,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(methods)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(be(annotatedWithAll(requiredAnnotationTypes, true)))
            .as("Methods %s should be annotated with %s".formatted(
                methods.getDescription(), String.join(", ", requiredAnnotationTypes))),
        configuration));
  }

  /**
   * Adds a rule enforcing that methods declared in classes matching the given criteria
   * must be annotated with all required annotations.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param requiredAnnotationTypes the required annotation type names
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldBeAnnotatedWithAllForClass(
      DescribedPredicate<? super JavaClass> classes, Collection<String> requiredAnnotationTypes) {
    return methodsShouldBeAnnotatedWithAllForClass(classes, requiredAnnotationTypes,
        defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldBeAnnotatedWithAllForClass(DescribedPredicate, Collection)}, with a
   * custom configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param requiredAnnotationTypes the required annotation type names
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldBeAnnotatedWithAllForClass(
      DescribedPredicate<? super JavaClass> classes, Collection<String> requiredAnnotationTypes,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().areDeclaredInClassesThat(classes)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(be(annotatedWithAll(requiredAnnotationTypes, true)))
            .as("Methods declared in classes %s should be annotated with %s".formatted(
                classes.getDescription(), String.join(", ", requiredAnnotationTypes))),
        configuration));
  }

  /**
   * Adds a rule enforcing that methods matching the given criteria
   * must not be annotated with a specific annotation.
   *
   * @param methods the methods to check, selected with {@link com.enofex.taikai.MethodCriteria}
   *     or an ArchUnit predicate
   * @param annotationType the forbidden annotation
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotBeAnnotatedWith(
      DescribedPredicate<? super JavaMethod> methods, Class<? extends Annotation> annotationType) {
    return methodsShouldNotBeAnnotatedWith(methods, annotationType.getName(),
        defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotBeAnnotatedWith(DescribedPredicate, Class)}, with a custom
   * configuration.
   *
   * @param methods the methods to check
   * @param annotationType the forbidden annotation
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotBeAnnotatedWith(
      DescribedPredicate<? super JavaMethod> methods, Class<? extends Annotation> annotationType,
      Configuration configuration) {
    return methodsShouldNotBeAnnotatedWith(methods, annotationType.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that methods matching the given criteria
   * must not be annotated with a specific annotation by name.
   *
   * @param methods the methods to check, selected with {@link com.enofex.taikai.MethodCriteria}
   *     or an ArchUnit predicate
   * @param annotationType the forbidden annotation type name
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotBeAnnotatedWith(
      DescribedPredicate<? super JavaMethod> methods, String annotationType) {
    return methodsShouldNotBeAnnotatedWith(methods, annotationType, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotBeAnnotatedWith(DescribedPredicate, String)}, with a custom
   * configuration.
   *
   * @param methods the methods to check
   * @param annotationType the forbidden annotation type name
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotBeAnnotatedWith(
      DescribedPredicate<? super JavaMethod> methods, String annotationType,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that(methods)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().notBeMetaAnnotatedWith(annotationType)
        .as("Methods %s should not be annotated with %s".formatted(methods.getDescription(),
            annotationType)), configuration));
  }

  /**
   * Adds a rule enforcing that methods declared in classes matching the given criteria
   * must not be annotated with a specific annotation.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param annotationType the forbidden annotation
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotBeAnnotatedWithForClass(
      DescribedPredicate<? super JavaClass> classes, Class<? extends Annotation> annotationType) {
    return methodsShouldNotBeAnnotatedWithForClass(classes, annotationType.getName(),
        defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotBeAnnotatedWithForClass(DescribedPredicate, Class)}, with a custom
   * configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param annotationType the forbidden annotation
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotBeAnnotatedWithForClass(
      DescribedPredicate<? super JavaClass> classes, Class<? extends Annotation> annotationType,
      Configuration configuration) {
    return methodsShouldNotBeAnnotatedWithForClass(classes, annotationType.getName(),
        configuration);
  }

  /**
   * Adds a rule enforcing that methods declared in classes matching the given criteria
   * must not be annotated with a specific annotation by name.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param annotationType the forbidden annotation type name
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotBeAnnotatedWithForClass(
      DescribedPredicate<? super JavaClass> classes, String annotationType) {
    return methodsShouldNotBeAnnotatedWithForClass(classes, annotationType, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotBeAnnotatedWithForClass(DescribedPredicate, String)}, with a custom
   * configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param annotationType the forbidden annotation type name
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotBeAnnotatedWithForClass(
      DescribedPredicate<? super JavaClass> classes, String annotationType,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().areDeclaredInClassesThat(classes)
        .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should().notBeMetaAnnotatedWith(annotationType)
        .as("Methods declared in classes %s should not be annotated with %s".formatted(
            classes.getDescription(), annotationType)), configuration));
  }

  /**
   * Adds a rule enforcing that methods matching the given criteria must have all specified
   * modifiers. Synthetic methods generated by the compiler are ignored.
   *
   * @param methods the methods to check, selected with {@link com.enofex.taikai.MethodCriteria}, for
   *     example {@code MethodCriteria.nameMatching("find.*")}, or an ArchUnit predicate
   * @param requiredModifiers the modifiers that must be present
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldHaveModifiers(
      DescribedPredicate<? super JavaMethod> methods, Collection<JavaModifier> requiredModifiers) {
    return methodsShouldHaveModifiers(methods, requiredModifiers, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldHaveModifiers(DescribedPredicate, Collection)}, with a custom
   * configuration.
   *
   * @param methods the methods to check
   * @param requiredModifiers the modifiers that must be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldHaveModifiers(
      DescribedPredicate<? super JavaMethod> methods, Collection<JavaModifier> requiredModifiers,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(methods)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(hasMethodsModifiers(requiredModifiers))
            .as("Methods %s should have all of these modifiers %s".formatted(
                methods.getDescription(),
                requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that all methods declared in classes matching the given criteria
   * must have the specified modifiers.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param requiredModifiers the modifiers that must be present
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldHaveModifiersForClass(
      DescribedPredicate<? super JavaClass> classes, Collection<JavaModifier> requiredModifiers) {
    return methodsShouldHaveModifiersForClass(classes, requiredModifiers, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldHaveModifiersForClass(DescribedPredicate, Collection)}, with a
   * custom configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param requiredModifiers the modifiers that must be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldHaveModifiersForClass(
      DescribedPredicate<? super JavaClass> classes, Collection<JavaModifier> requiredModifiers,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().areDeclaredInClassesThat(classes)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(hasMethodsModifiers(requiredModifiers))
            .as("Methods declared in classes %s should have all of these modifiers %s".formatted(
                classes.getDescription(),
                requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that methods matching the given criteria must not have any of the
   * specified modifiers. Synthetic methods generated by the compiler are ignored.
   *
   * @param methods the methods to check, selected with {@link com.enofex.taikai.MethodCriteria}, for
   *     example {@code MethodCriteria.nameMatching("find.*")}, or an ArchUnit predicate
   * @param forbiddenModifiers the modifiers that must not be present
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotHaveModifiers(
      DescribedPredicate<? super JavaMethod> methods, Collection<JavaModifier> forbiddenModifiers) {
    return methodsShouldNotHaveModifiers(methods, forbiddenModifiers, defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotHaveModifiers(DescribedPredicate, Collection)}, with a custom
   * configuration.
   *
   * @param methods the methods to check
   * @param forbiddenModifiers the modifiers that must not be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.MethodCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotHaveModifiers(
      DescribedPredicate<? super JavaMethod> methods, Collection<JavaModifier> forbiddenModifiers,
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(methods)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(notHasMethodModifiers(forbiddenModifiers))
            .as("Methods %s should not have any of these modifiers %s".formatted(
                methods.getDescription(),
                forbiddenModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that all methods declared in classes matching the given criteria
   * must not have the specified modifiers.
   *
   * @param classes the classes whose declared methods are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param forbiddenModifiers the modifiers that must not be present
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotHaveModifiersForClass(
      DescribedPredicate<? super JavaClass> classes,
      Collection<JavaModifier> forbiddenModifiers) {
    return methodsShouldNotHaveModifiersForClass(classes, forbiddenModifiers,
        defaultConfiguration());
  }

  /**
   * See {@link #methodsShouldNotHaveModifiersForClass(DescribedPredicate, Collection)}, with a
   * custom configuration.
   *
   * @param classes the classes whose declared methods are checked
   * @param forbiddenModifiers the modifiers that must not be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer methodsShouldNotHaveModifiersForClass(
      DescribedPredicate<? super JavaClass> classes,
      Collection<JavaModifier> forbiddenModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that().areDeclaredInClassesThat(classes)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(notHasMethodModifiers(forbiddenModifiers))
            .as("Methods declared in classes %s should not have any of these modifiers %s".formatted(
                classes.getDescription(),
                forbiddenModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule ensuring methods do not exceed a specific number of parameters.
   *
   * @param maxMethodParameters maximum number of allowed parameters
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer methodsShouldNotExceedMaxParameters(int maxMethodParameters) {
    return methodsShouldNotExceedMaxParameters(maxMethodParameters, defaultConfiguration());
  }

  /**
   * Adds a rule ensuring methods do not exceed a specific number of parameters,
   * using a custom configuration.
   *
   * @param maxMethodParameters maximum number of allowed parameters
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer methodsShouldNotExceedMaxParameters(int maxMethodParameters, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(notExceedMaxParameters(maxMethodParameters))
        .as("Methods should not have more than %d parameters".formatted(maxMethodParameters)), configuration));
  }

  /**
   * Adds a rule prohibiting usage of deprecated APIs.
   *
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer noUsageOfDeprecatedAPIs() {
    return noUsageOfDeprecatedAPIs(defaultConfiguration());
  }

  /**
   * Adds a rule prohibiting usage of deprecated APIs, using a custom configuration.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
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
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer classesShouldResideInPackage(String packageIdentifier) {
    return classesShouldResideInPackage(packageIdentifier, defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that all classes reside in a specific package,
   * using a custom configuration.
   *
   * @param packageIdentifier the expected package pattern
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer classesShouldResideInPackage(String packageIdentifier,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .should().resideInAPackage(packageIdentifier)
            .as("Classes should reside in package %s".formatted(packageIdentifier)),
        configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * must reside in a specific package.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param packageIdentifier the required package
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldResideInPackage(DescribedPredicate<? super JavaClass> classes,
      String packageIdentifier) {
    return classesShouldResideInPackage(classes, packageIdentifier, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldResideInPackage(DescribedPredicate, String)}, with a custom
   * configuration.
   *
   * @param classes the classes to check
   * @param packageIdentifier the required package
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldResideInPackage(DescribedPredicate<? super JavaClass> classes,
      String packageIdentifier, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(classes)
        .should().resideInAPackage(packageIdentifier)
        .as("Classes %s should reside in package %s".formatted(
            classes.getDescription(), packageIdentifier)), configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * must reside outside a specific package.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param packageIdentifier the forbidden package
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldResideOutsidePackage(
      DescribedPredicate<? super JavaClass> classes, String packageIdentifier) {
    return classesShouldResideOutsidePackage(classes, packageIdentifier, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldResideOutsidePackage(DescribedPredicate, String)}, with a custom
   * configuration.
   *
   * @param classes the classes to check
   * @param packageIdentifier the forbidden package
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldResideOutsidePackage(
      DescribedPredicate<? super JavaClass> classes, String packageIdentifier,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(classes)
        .should().resideOutsideOfPackage(packageIdentifier)
        .as("Classes %s should reside outside package %s".formatted(
            classes.getDescription(), packageIdentifier)), configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * must be declared as Java records.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeRecords(DescribedPredicate<? super JavaClass> classes) {
    return classesShouldBeRecords(classes, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldBeRecords(DescribedPredicate)}, with a custom configuration.
   *
   * @param classes the classes to check
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeRecords(DescribedPredicate<? super JavaClass> classes,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(classes)
            .should().beRecords()
            .as("Classes %s should be records".formatted(classes.getDescription())),
        configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * must be declared as Java interfaces.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeInterfaces(DescribedPredicate<? super JavaClass> classes) {
    return classesShouldBeInterfaces(classes, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldBeInterfaces(DescribedPredicate)}, with a custom configuration.
   *
   * @param classes the classes to check
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeInterfaces(DescribedPredicate<? super JavaClass> classes,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(classes)
            .should().beInterfaces()
            .as("Classes %s should be interfaces".formatted(classes.getDescription())),
        configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * must be annotated with all required annotations.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param requiredAnnotationTypes the required annotation type names
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeAnnotatedWithAll(
      DescribedPredicate<? super JavaClass> classes, Collection<String> requiredAnnotationTypes) {
    return classesShouldBeAnnotatedWithAll(classes, requiredAnnotationTypes,
        defaultConfiguration());
  }

  /**
   * See {@link #classesShouldBeAnnotatedWithAll(DescribedPredicate, Collection)}, with a custom
   * configuration.
   *
   * @param classes the classes to check
   * @param requiredAnnotationTypes the required annotation type names
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeAnnotatedWithAll(
      DescribedPredicate<? super JavaClass> classes, Collection<String> requiredAnnotationTypes,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(classes)
            .should(be(annotatedWithAll(requiredAnnotationTypes, true)))
            .as("Classes %s should be annotated with %s".formatted(
                classes.getDescription(), String.join(", ", requiredAnnotationTypes))),
        configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria must have at least one field
   * matching the given field criteria. Fields inherited from superclasses count, so a field
   * declared in a shared base class satisfies the rule for all its subclasses. Synthetic fields
   * generated by the compiler are ignored.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param requiredFields the field that must be present, selected with
   *     {@link com.enofex.taikai.FieldCriteria}, for example
   *     {@code FieldCriteria.annotatedWith("jakarta.persistence.Version")}, or an ArchUnit
   *     predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.enofex.taikai.FieldCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldHaveField(
      DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaField> requiredFields) {
    return classesShouldHaveField(classes, requiredFields, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldHaveField(DescribedPredicate, DescribedPredicate)}, with a custom
   * configuration.
   *
   * @param classes the classes to check
   * @param requiredFields the field that must be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.enofex.taikai.FieldCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldHaveField(
      DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaField> requiredFields, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(classes)
            .should(haveFieldMatching(requiredFields))
            .as("Classes %s should have a field %s".formatted(
                classes.getDescription(), requiredFields.getDescription())),
        configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * must be annotated with a specific annotation.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param annotationType the required annotation
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeAnnotatedWith(DescribedPredicate<? super JavaClass> classes,
      Class<? extends Annotation> annotationType) {
    return classesShouldBeAnnotatedWith(classes, annotationType.getName(), defaultConfiguration());
  }

  /**
   * See {@link #classesShouldBeAnnotatedWith(DescribedPredicate, Class)}, with a custom
   * configuration.
   *
   * @param classes the classes to check
   * @param annotationType the required annotation
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeAnnotatedWith(DescribedPredicate<? super JavaClass> classes,
      Class<? extends Annotation> annotationType, Configuration configuration) {
    return classesShouldBeAnnotatedWith(classes, annotationType.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * must be annotated with a specific annotation by name.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param annotationType the required annotation type name
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeAnnotatedWith(DescribedPredicate<? super JavaClass> classes,
      String annotationType) {
    return classesShouldBeAnnotatedWith(classes, annotationType, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldBeAnnotatedWith(DescribedPredicate, String)}, with a custom
   * configuration.
   *
   * @param classes the classes to check
   * @param annotationType the required annotation type name
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeAnnotatedWith(DescribedPredicate<? super JavaClass> classes,
      String annotationType, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(classes)
        .should().beMetaAnnotatedWith(annotationType)
        .as("Classes %s should be annotated with %s".formatted(classes.getDescription(),
            annotationType)), configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * must not be annotated with a specific annotation.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param annotationType the forbidden annotation
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldNotBeAnnotatedWith(
      DescribedPredicate<? super JavaClass> classes, Class<? extends Annotation> annotationType) {
    return classesShouldNotBeAnnotatedWith(classes, annotationType.getName(),
        defaultConfiguration());
  }

  /**
   * See {@link #classesShouldNotBeAnnotatedWith(DescribedPredicate, Class)}, with a custom
   * configuration.
   *
   * @param classes the classes to check
   * @param annotationType the forbidden annotation
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldNotBeAnnotatedWith(
      DescribedPredicate<? super JavaClass> classes, Class<? extends Annotation> annotationType,
      Configuration configuration) {
    return classesShouldNotBeAnnotatedWith(classes, annotationType.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * must not be annotated with a specific annotation by name.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param annotationType the forbidden annotation type name
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldNotBeAnnotatedWith(
      DescribedPredicate<? super JavaClass> classes, String annotationType) {
    return classesShouldNotBeAnnotatedWith(classes, annotationType, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldNotBeAnnotatedWith(DescribedPredicate, String)}, with a custom
   * configuration.
   *
   * @param classes the classes to check
   * @param annotationType the forbidden annotation type name
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldNotBeAnnotatedWith(
      DescribedPredicate<? super JavaClass> classes, String annotationType,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(classes)
        .should().notBeMetaAnnotatedWith(annotationType)
        .as("Classes %s should not be annotated with %s".formatted(classes.getDescription(),
            annotationType)), configuration));
  }

  /**
   * Adds a rule enforcing that all classes implement both {@code hashCode()} and {@code equals()}.
   *
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer classesShouldImplementHashCodeAndEquals() {
    return classesShouldImplementHashCodeAndEquals(defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that all classes implement both {@code hashCode()} and {@code equals()},
   * using a custom configuration.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer classesShouldImplementHashCodeAndEquals(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(implementHashCodeAndEquals())
        .as("Classes should implement hashCode and equals"), configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * should be assignable to a specific class type.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param clazz the target class type
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeAssignableTo(DescribedPredicate<? super JavaClass> classes,
      Class<?> clazz) {
    return classesShouldBeAssignableTo(classes, clazz.getName(), defaultConfiguration());
  }

  /**
   * See {@link #classesShouldBeAssignableTo(DescribedPredicate, Class)}, with a custom
   * configuration.
   *
   * @param classes the classes to check
   * @param clazz the target class type
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeAssignableTo(DescribedPredicate<? super JavaClass> classes,
      Class<?> clazz, Configuration configuration) {
    return classesShouldBeAssignableTo(classes, clazz.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * should be assignable to a specific type by name.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param typeName the target type name
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeAssignableTo(DescribedPredicate<? super JavaClass> classes,
      String typeName) {
    return classesShouldBeAssignableTo(classes, typeName, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldBeAssignableTo(DescribedPredicate, String)}, with a custom
   * configuration.
   *
   * @param classes the classes to check
   * @param typeName the target type name
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBeAssignableTo(DescribedPredicate<? super JavaClass> classes,
      String typeName, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(classes)
        .should().beAssignableTo(typeName)
        .as("Classes %s should be assignable to %s".formatted(
            classes.getDescription(), typeName)), configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * should implement a specific interface type.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param clazz the interface class
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldImplement(DescribedPredicate<? super JavaClass> classes,
      Class<?> clazz) {
    return classesShouldImplement(classes, clazz.getName(), defaultConfiguration());
  }

  /**
   * See {@link #classesShouldImplement(DescribedPredicate, Class)}, with a custom configuration.
   *
   * @param classes the classes to check
   * @param clazz the interface class
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldImplement(DescribedPredicate<? super JavaClass> classes,
      Class<?> clazz, Configuration configuration) {
    return classesShouldImplement(classes, clazz.getName(), configuration);
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * should implement a specific interface type by name.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param typeName the interface type name
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldImplement(DescribedPredicate<? super JavaClass> classes,
      String typeName) {
    return classesShouldImplement(classes, typeName, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldImplement(DescribedPredicate, String)}, with a custom configuration.
   *
   * @param classes the classes to check
   * @param typeName the interface type name
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldImplement(DescribedPredicate<? super JavaClass> classes,
      String typeName, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(classes)
        .should().implement(typeName)
        .as("Classes %s should implement %s".formatted(
            classes.getDescription(), typeName)), configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * must have all specified modifiers.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param requiredModifiers the modifiers that must be present
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldHaveModifiers(DescribedPredicate<? super JavaClass> classes,
      Collection<JavaModifier> requiredModifiers) {
    return classesShouldHaveModifiers(classes, requiredModifiers, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldHaveModifiers(DescribedPredicate, Collection)}, with a custom
   * configuration.
   *
   * @param classes the classes to check
   * @param requiredModifiers the modifiers that must be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldHaveModifiers(DescribedPredicate<? super JavaClass> classes,
      Collection<JavaModifier> requiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(classes)
            .should(hasClassModifiers(requiredModifiers))
            .as("Classes %s should have all of these modifiers %s".formatted(
                classes.getDescription(),
                requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria
   * must not have any of the specified modifiers.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @param forbiddenModifiers the modifiers that must not be present
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldNotHaveModifiers(DescribedPredicate<? super JavaClass> classes,
      Collection<JavaModifier> forbiddenModifiers) {
    return classesShouldNotHaveModifiers(classes, forbiddenModifiers, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldNotHaveModifiers(DescribedPredicate, Collection)}, with a custom
   * configuration.
   *
   * @param classes the classes to check
   * @param forbiddenModifiers the modifiers that must not be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldNotHaveModifiers(DescribedPredicate<? super JavaClass> classes,
      Collection<JavaModifier> forbiddenModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(classes)
            .should(notHasClassModifiers(forbiddenModifiers))
            .as("Classes %s should not have any of these modifiers %s".formatted(
                classes.getDescription(),
                forbiddenModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that classes matching the given criteria should be package-private.
   *
   * @param classes the classes to check, selected with {@link com.enofex.taikai.ClassCriteria}
   *     or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBePackagePrivate(
      DescribedPredicate<? super JavaClass> classes) {
    return classesShouldBePackagePrivate(classes, defaultConfiguration());
  }

  /**
   * See {@link #classesShouldBePackagePrivate(DescribedPredicate)}, with a custom configuration.
   *
   * @param classes the classes to check
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer classesShouldBePackagePrivate(DescribedPredicate<? super JavaClass> classes,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(classes)
            .should().bePackagePrivate()
            .as("Classes %s should be package-private".formatted(classes.getDescription())),
        configuration));
  }

  /**
   * Adds a rule enforcing that fields should not be {@code public}
   * unless they are also {@code static}.
   *
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer fieldsShouldNotBePublic() {
    return fieldsShouldNotBePublic(defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that fields should not be {@code public}
   * unless they are also {@code static}, using a custom configuration.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer fieldsShouldNotBePublic(Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that().doNotHaveModifier(JavaModifier.SYNTHETIC)
        .should(notBePublicUnlessStatic())
        .as("Fields should not be public unless they are static"), configuration));
  }

  /**
   * Adds a rule enforcing that fields matching the given criteria must have all specified
   * modifiers. Synthetic fields generated by the compiler are ignored.
   *
   * @param fields the fields to check, selected with {@link com.enofex.taikai.FieldCriteria}, for
   *     example {@code FieldCriteria.nameMatching("logger")}, or an ArchUnit predicate
   * @param requiredModifiers the modifiers that must be present
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.FieldCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer fieldsShouldHaveModifiers(
      DescribedPredicate<? super JavaField> fields, Collection<JavaModifier> requiredModifiers) {
    return fieldsShouldHaveModifiers(fields, requiredModifiers, defaultConfiguration());
  }

  /**
   * See {@link #fieldsShouldHaveModifiers(DescribedPredicate, Collection)}, with a custom
   * configuration.
   *
   * @param fields the fields to check
   * @param requiredModifiers the modifiers that must be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.FieldCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer fieldsShouldHaveModifiers(
      DescribedPredicate<? super JavaField> fields, Collection<JavaModifier> requiredModifiers,
      Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
            .that(fields)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(hasFieldModifiers(requiredModifiers))
            .as("Fields %s should have all of these modifiers %s".formatted(
                fields.getDescription(),
                requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that all fields declared in classes matching the given criteria
   * must have the specified modifiers. Synthetic fields generated by the compiler are ignored.
   *
   * @param classes the classes whose declared fields are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param requiredModifiers the modifiers that must be present
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer fieldsShouldHaveModifiersForClass(
      DescribedPredicate<? super JavaClass> classes, Collection<JavaModifier> requiredModifiers) {
    return fieldsShouldHaveModifiersForClass(classes, requiredModifiers, defaultConfiguration());
  }

  /**
   * See {@link #fieldsShouldHaveModifiersForClass(DescribedPredicate, Collection)}, with a custom
   * configuration.
   *
   * @param classes the classes whose declared fields are checked
   * @param requiredModifiers the modifiers that must be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer fieldsShouldHaveModifiersForClass(
      DescribedPredicate<? super JavaClass> classes, Collection<JavaModifier> requiredModifiers,
      Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
            .that().areDeclaredInClassesThat(classes)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(hasFieldModifiers(requiredModifiers))
            .as("Fields declared in classes %s should have all of these modifiers %s".formatted(
                classes.getDescription(),
                requiredModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that fields matching the given criteria must not have any of the
   * specified modifiers. Synthetic fields generated by the compiler are ignored.
   *
   * @param fields the fields to check, selected with {@link com.enofex.taikai.FieldCriteria}, for
   *     example {@code FieldCriteria.nameMatching("logger")}, or an ArchUnit predicate
   * @param forbiddenModifiers the modifiers that must not be present
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.FieldCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer fieldsShouldNotHaveModifiers(
      DescribedPredicate<? super JavaField> fields, Collection<JavaModifier> forbiddenModifiers) {
    return fieldsShouldNotHaveModifiers(fields, forbiddenModifiers, defaultConfiguration());
  }

  /**
   * See {@link #fieldsShouldNotHaveModifiers(DescribedPredicate, Collection)}, with a custom
   * configuration.
   *
   * @param fields the fields to check
   * @param forbiddenModifiers the modifiers that must not be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.FieldCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer fieldsShouldNotHaveModifiers(
      DescribedPredicate<? super JavaField> fields, Collection<JavaModifier> forbiddenModifiers,
      Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
            .that(fields)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(notHasFieldModifiers(forbiddenModifiers))
            .as("Fields %s should not have any of these modifiers %s".formatted(
                fields.getDescription(),
                forbiddenModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Adds a rule enforcing that all fields declared in classes matching the given criteria
   * must not have the specified modifiers. Synthetic fields generated by the compiler are
   * ignored.
   *
   * @param classes the classes whose declared fields are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param forbiddenModifiers the modifiers that must not be present
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer fieldsShouldNotHaveModifiersForClass(
      DescribedPredicate<? super JavaClass> classes,
      Collection<JavaModifier> forbiddenModifiers) {
    return fieldsShouldNotHaveModifiersForClass(classes, forbiddenModifiers,
        defaultConfiguration());
  }

  /**
   * See {@link #fieldsShouldNotHaveModifiersForClass(DescribedPredicate, Collection)}, with a
   * custom configuration.
   *
   * @param classes the classes whose declared fields are checked
   * @param forbiddenModifiers the modifiers that must not be present
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer fieldsShouldNotHaveModifiersForClass(
      DescribedPredicate<? super JavaClass> classes,
      Collection<JavaModifier> forbiddenModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
            .that().areDeclaredInClassesThat(classes)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(notHasFieldModifiers(forbiddenModifiers))
            .as("Fields declared in classes %s should not have any of these modifiers %s".formatted(
                classes.getDescription(),
                forbiddenModifiers.stream().map(Enum::name).collect(Collectors.joining(", ")))),
        configuration));
  }

  /**
   * Prevents fields matching the given criteria from having matching types, including generic
   * arguments, bounds, and array components. Synthetic fields generated by the compiler are
   * ignored. Use {@link com.enofex.taikai.ClassCriteria} or ArchUnit predicates to select the
   * types.
   *
   * @param fields the fields to check, selected with {@link com.enofex.taikai.FieldCriteria}, for
   *     example {@code FieldCriteria.annotatedWith("jakarta.persistence.Id")}, or an ArchUnit
   *     predicate
   * @param forbiddenTypes the forbidden field types
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.FieldCriteria
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer fieldsShouldNotBeOfType(
      DescribedPredicate<? super JavaField> fields,
      DescribedPredicate<? super JavaClass> forbiddenTypes) {
    return fieldsShouldNotBeOfType(fields, forbiddenTypes, defaultConfiguration());
  }

  /**
   * See {@link #fieldsShouldNotBeOfType(DescribedPredicate, DescribedPredicate)}, with a custom
   * configuration.
   *
   * @param fields the fields to check
   * @param forbiddenTypes the forbidden field types
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.FieldCriteria
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer fieldsShouldNotBeOfType(
      DescribedPredicate<? super JavaField> fields,
      DescribedPredicate<? super JavaClass> forbiddenTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
            .that(fields)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(notHaveFieldTypesMatching(forbiddenTypes))
            .as("Fields %s should not be of types %s".formatted(
                fields.getDescription(), forbiddenTypes.getDescription())),
        configuration));
  }

  /**
   * Prevents fields declared in classes matching the given criteria from having matching types,
   * including generic arguments, bounds, and array components. Synthetic fields generated by the
   * compiler are ignored. Use {@link com.enofex.taikai.ClassCriteria} or ArchUnit predicates to
   * select the types.
   *
   * @param classes the classes whose declared fields are checked, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param forbiddenTypes the forbidden field types
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer fieldsShouldNotBeOfTypeForClass(
      DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> forbiddenTypes) {
    return fieldsShouldNotBeOfTypeForClass(classes, forbiddenTypes, defaultConfiguration());
  }

  /**
   * See {@link #fieldsShouldNotBeOfTypeForClass(DescribedPredicate, DescribedPredicate)}, with a
   * custom configuration.
   *
   * @param classes the classes whose declared fields are checked
   * @param forbiddenTypes the forbidden field types
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer fieldsShouldNotBeOfTypeForClass(
      DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> forbiddenTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
            .that().areDeclaredInClassesThat(classes)
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should(notHaveFieldTypesMatching(forbiddenTypes))
            .as("Fields declared in classes %s should not be of types %s".formatted(
                classes.getDescription(), forbiddenTypes.getDescription())),
        configuration));
  }

  /**
   * Adds a rule prohibiting usage of a specific class.
   *
   * @param clazz the class type that must not be used
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer noUsageOf(Class<?> clazz) {
    return noUsageOf(clazz.getName(), defaultConfiguration());
  }

  /**
   * Adds a rule prohibiting usage of a specific class, with a custom configuration.
   *
   * @param clazz the class type that must not be used
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer noUsageOf(Class<?> clazz, Configuration configuration) {
    return noUsageOf(clazz.getName(), configuration);
  }

  /**
   * Adds a rule prohibiting usage of a specific type by name.
   *
   * @param typeName the fully qualified class name that must not be used
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer noUsageOf(String typeName) {
    return noUsageOf(typeName, defaultConfiguration());
  }

  /**
   * Adds a rule prohibiting usage of a specific type by name,
   * using a custom configuration.
   *
   * @param typeName the fully qualified class name that must not be used
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer noUsageOf(String typeName, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .should().dependOnClassesThat().areAssignableTo(typeName)
        .as("Classes %s should not be used".formatted(typeName)), configuration));
  }

  /**
   * Adds a rule prohibiting usage of a specific class by classes matching the given criteria.
   *
   * @param classes the classes in which usage is disallowed, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param clazz the class type that must not be used, including its subtypes
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer noUsageOf(DescribedPredicate<? super JavaClass> classes, Class<?> clazz) {
    return noUsageOf(classes, clazz.getName(), defaultConfiguration());
  }

  /**
   * See {@link #noUsageOf(DescribedPredicate, Class)}, with a custom configuration.
   *
   * @param classes the classes in which usage is disallowed
   * @param clazz the class type that must not be used, including its subtypes
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer noUsageOf(DescribedPredicate<? super JavaClass> classes, Class<?> clazz,
      Configuration configuration) {
    return noUsageOf(classes, clazz.getName(), configuration);
  }

  /**
   * Adds a rule prohibiting usage of a specific type by name by classes matching the given
   * criteria.
   *
   * @param classes the classes in which usage is disallowed, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param typeName the fully qualified class name that must not be used, including its subtypes
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer noUsageOf(DescribedPredicate<? super JavaClass> classes, String typeName) {
    return noUsageOf(classes, typeName, defaultConfiguration());
  }

  /**
   * See {@link #noUsageOf(DescribedPredicate, String)}, with a custom configuration.
   *
   * @param classes the classes in which usage is disallowed
   * @param typeName the fully qualified class name that must not be used, including its subtypes
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer noUsageOf(DescribedPredicate<? super JavaClass> classes, String typeName,
      Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .that(classes)
        .should().dependOnClassesThat().areAssignableTo(typeName)
        .as("Classes %s should not use %s".formatted(classes.getDescription(), typeName)),
        configuration));
  }

  /**
   * Adds a rule prohibiting usage of any type matching the given criteria. Classes that match
   * the criteria themselves are not checked, so the classes of a banned package may still use
   * each other.
   *
   * @param forbiddenTypes the types that must not be used, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer noUsageOf(DescribedPredicate<? super JavaClass> forbiddenTypes) {
    return noUsageOf(forbiddenTypes, defaultConfiguration());
  }

  /**
   * See {@link #noUsageOf(DescribedPredicate)}, with a custom configuration.
   *
   * @param forbiddenTypes the types that must not be used
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer noUsageOf(DescribedPredicate<? super JavaClass> forbiddenTypes,
      Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .that(DescribedPredicate.not(forbiddenTypes))
        .should().dependOnClassesThat(forbiddenTypes)
        .as("Classes %s should not be used".formatted(forbiddenTypes.getDescription())),
        configuration));
  }

  /**
   * Adds a rule prohibiting classes matching the given criteria from using any type matching
   * other criteria. Selected classes that match the forbidden criteria themselves are not
   * checked, so the classes of a banned package may still use each other.
   *
   * @param classes the classes in which usage is disallowed, selected with
   *     {@link com.enofex.taikai.ClassCriteria} or an ArchUnit predicate
   * @param forbiddenTypes the types that must not be used
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer noUsageOf(DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> forbiddenTypes) {
    return noUsageOf(classes, forbiddenTypes, defaultConfiguration());
  }

  /**
   * See {@link #noUsageOf(DescribedPredicate, DescribedPredicate)}, with a custom configuration.
   *
   * @param classes the classes in which usage is disallowed
   * @param forbiddenTypes the types that must not be used
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   * @see com.enofex.taikai.ClassCriteria
   * @see com.tngtech.archunit.base.DescribedPredicate
   */
  public JavaConfigurer noUsageOf(DescribedPredicate<? super JavaClass> classes,
      DescribedPredicate<? super JavaClass> forbiddenTypes, Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
        .that(classes)
        .and(DescribedPredicate.not(forbiddenTypes))
        .should().dependOnClassesThat(forbiddenTypes)
        .as("Classes %s should not use classes %s".formatted(
            classes.getDescription(), forbiddenTypes.getDescription())), configuration));
  }

  /**
   * Adds a rule prohibiting the usage of {@code System.out} and {@code System.err}.
   *
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer noUsageOfSystemOutOrErr() {
    return noUsageOfSystemOutOrErr(defaultConfiguration());
  }

  /**
   * Adds a rule prohibiting the usage of {@code System.out} and {@code System.err},
   * using a custom configuration.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer noUsageOfSystemOutOrErr(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(notUseSystemOutOrErr())
        .as("Classes should not use System.out or System.err"), configuration));
  }

  /**
   * Adds a rule enforcing that {@code final} classes must not have any protected members.
   *
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer finalClassesShouldNotHaveProtectedMembers() {
    return finalClassesShouldNotHaveProtectedMembers(defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that {@code final} classes must not have any protected members,
   * using a custom configuration.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
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
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer serialVersionUIDFieldsShouldBeStaticFinalLong() {
    return serialVersionUIDFieldsShouldBeStaticFinalLong(defaultConfiguration());
  }

  /**
   * Adds a rule enforcing that {@code serialVersionUID} fields
   * must be {@code static final long}, using a custom configuration.
   *
   * @param configuration the rule configuration
   * @return this configurer for fluent chaining
   */
  public JavaConfigurer serialVersionUIDFieldsShouldBeStaticFinalLong(Configuration configuration) {
    return addRule(TaikaiRule.of(fields()
        .that(namedSerialVersionUID())
        .should(beStaticFinalLong())
        .as("serialVersionUID should be static final long"), configuration));
  }

  @Override
  public JavaConfigurer disable() {
    disable(JavaConfigurer.class);
    disable(ImportsConfigurer.class);
    disable(NamingConfigurer.class);

    return this;
  }
}
