package com.enofex.taikai.test;

import static com.enofex.taikai.internal.ArchConditions.notDeclareThrownExceptions;
import static com.enofex.taikai.test.ContainAssertionsOrVerifications.containAssertionsOrVerifications;
import static com.enofex.taikai.test.JUnit5DescribedPredicates.ANNOTATION_DISABLED;
import static com.enofex.taikai.test.JUnit5DescribedPredicates.ANNOTATION_DISPLAY_NAME;
import static com.enofex.taikai.test.JUnit5DescribedPredicates.ANNOTATION_PARAMETRIZED_TEST;
import static com.enofex.taikai.test.JUnit5DescribedPredicates.ANNOTATION_TEST;
import static com.enofex.taikai.test.JUnit5DescribedPredicates.annotatedWithTestOrParameterizedTest;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.enofex.taikai.Namespace.IMPORT;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;

public final class JUnit5Configurer extends AbstractConfigurer {

  private static final Configuration CONFIGURATION = Configuration.of(IMPORT.ONLY_TESTS);

  JUnit5Configurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public JUnit5Configurer methodsShouldMatch(String regex) {
    return methodsShouldMatch(regex, CONFIGURATION);
  }

  public JUnit5Configurer methodsShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should().haveNameMatching(regex)
            .as("Methods annotated with %s or %s should have names matching %s".formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETRIZED_TEST, regex)),
        configuration));
  }

  public JUnit5Configurer methodsShouldNotDeclareExceptions() {
    return methodsShouldNotDeclareExceptions(CONFIGURATION);
  }

  public JUnit5Configurer methodsShouldNotDeclareExceptions(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should(notDeclareThrownExceptions())
            .as("Methods annotated with %s or %s should not declare thrown Exceptions".formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETRIZED_TEST)),
        configuration));
  }

  public JUnit5Configurer methodsShouldBeAnnotatedWithDisplayName() {
    return methodsShouldBeAnnotatedWithDisplayName(CONFIGURATION);
  }

  public JUnit5Configurer methodsShouldBeAnnotatedWithDisplayName(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should().beMetaAnnotatedWith(ANNOTATION_DISPLAY_NAME)
            .as("Methods annotated with %s or %s should be annotated with %s".formatted(ANNOTATION_TEST,
                ANNOTATION_PARAMETRIZED_TEST, ANNOTATION_DISPLAY_NAME)),
        configuration));
  }

  public JUnit5Configurer methodsShouldBePackagePrivate() {
    return methodsShouldBePackagePrivate(CONFIGURATION);
  }

  public JUnit5Configurer methodsShouldBePackagePrivate(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should().bePackagePrivate()
            .as("Methods annotated with %s or %s should be package-private".formatted(ANNOTATION_TEST,
                ANNOTATION_PARAMETRIZED_TEST)),
        configuration));
  }

  public JUnit5Configurer methodsShouldNotBeAnnotatedWithDisabled() {
    return methodsShouldNotBeAnnotatedWithDisabled(CONFIGURATION);
  }

  public JUnit5Configurer methodsShouldNotBeAnnotatedWithDisabled(Configuration configuration) {
    return addRule(TaikaiRule.of(noMethods()
            .should().beMetaAnnotatedWith(ANNOTATION_DISABLED)
            .as("Methods should not be annotated with %s".formatted(ANNOTATION_DISABLED)),
        configuration));
  }

  public JUnit5Configurer methodsShouldContainAssertionsOrVerifications() {
    return methodsShouldContainAssertionsOrVerifications(CONFIGURATION);
  }

  public JUnit5Configurer methodsShouldContainAssertionsOrVerifications(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
        .that(are(annotatedWithTestOrParameterizedTest(true)))
        .should(containAssertionsOrVerifications()), configuration));
  }

  public JUnit5Configurer classesShouldNotBeAnnotatedWithDisabled() {
    return classesShouldNotBeAnnotatedWithDisabled(CONFIGURATION);
  }

  public JUnit5Configurer classesShouldBePackagePrivate(String regex) {
    return classesShouldBePackagePrivate(regex, CONFIGURATION);
  }

  public JUnit5Configurer classesShouldBePackagePrivate(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areNotInterfaces().and().haveNameMatching(regex)
            .should().bePackagePrivate()
            .as("Classes with names matching %s should be package-private".formatted(regex)),
        configuration));
  }


  public JUnit5Configurer classesShouldNotBeAnnotatedWithDisabled(Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
            .should().beMetaAnnotatedWith(ANNOTATION_DISABLED)
            .as("Classes should not be annotated with %s".formatted(ANNOTATION_DISABLED)),
        configuration));
  }
}