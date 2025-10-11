package com.enofex.taikai.test;

import static com.enofex.taikai.internal.ArchConditions.notDeclareThrownExceptions;
import static com.enofex.taikai.test.ContainAssertionsOrVerifications.containAssertionsOrVerifications;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_DISABLED;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_DISPLAY_NAME;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_PARAMETRIZED_TEST;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_TEST;
import static com.enofex.taikai.test.JUnitDescribedPredicates.annotatedWithTestOrParameterizedTest;
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
import com.enofex.taikai.configures.DisableableConfigurer;

public class JUnitConfigurer extends AbstractConfigurer {

  private static final Configuration CONFIGURATION = Configuration.of(IMPORT.ONLY_TESTS);

  JUnitConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public JUnitConfigurer methodsShouldMatch(String regex) {
    return methodsShouldMatch(regex, CONFIGURATION);
  }

  public JUnitConfigurer methodsShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should().haveNameMatching(regex)
            .as("Methods annotated with %s or %s should have names matching %s".formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETRIZED_TEST, regex)),
        configuration));
  }

  public JUnitConfigurer methodsShouldNotDeclareExceptions() {
    return methodsShouldNotDeclareExceptions(CONFIGURATION);
  }

  public JUnitConfigurer methodsShouldNotDeclareExceptions(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should(notDeclareThrownExceptions())
            .as("Methods annotated with %s or %s should not declare thrown Exceptions".formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETRIZED_TEST)),
        configuration));
  }

  public JUnitConfigurer methodsShouldBeAnnotatedWithDisplayName() {
    return methodsShouldBeAnnotatedWithDisplayName(CONFIGURATION);
  }

  public JUnitConfigurer methodsShouldBeAnnotatedWithDisplayName(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should().beMetaAnnotatedWith(ANNOTATION_DISPLAY_NAME)
            .as("Methods annotated with %s or %s should be annotated with %s".formatted(ANNOTATION_TEST,
                ANNOTATION_PARAMETRIZED_TEST, ANNOTATION_DISPLAY_NAME)),
        configuration));
  }

  public JUnitConfigurer methodsShouldBePackagePrivate() {
    return methodsShouldBePackagePrivate(CONFIGURATION);
  }

  public JUnitConfigurer methodsShouldBePackagePrivate(Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should().bePackagePrivate()
            .as("Methods annotated with %s or %s should be package-private".formatted(ANNOTATION_TEST,
                ANNOTATION_PARAMETRIZED_TEST)),
        configuration));
  }

  public JUnitConfigurer methodsShouldNotBeAnnotatedWithDisabled() {
    return methodsShouldNotBeAnnotatedWithDisabled(CONFIGURATION);
  }

  public JUnitConfigurer methodsShouldNotBeAnnotatedWithDisabled(Configuration configuration) {
    return addRule(TaikaiRule.of(noMethods()
            .should().beMetaAnnotatedWith(ANNOTATION_DISABLED)
            .as("Methods should not be annotated with %s".formatted(ANNOTATION_DISABLED)),
        configuration));
  }

  public JUnitConfigurer methodsShouldContainAssertionsOrVerifications() {
    return methodsShouldContainAssertionsOrVerifications(CONFIGURATION);
  }

  public JUnitConfigurer methodsShouldContainAssertionsOrVerifications(
      Configuration configuration) {
    return addRule(TaikaiRule.of(methods()
            .that(are(annotatedWithTestOrParameterizedTest(true)))
            .should(containAssertionsOrVerifications())
            .as("Methods annotated with %s or %s should contain assertions or verifications".formatted(
                ANNOTATION_TEST, ANNOTATION_PARAMETRIZED_TEST)),
        configuration));
  }

  public JUnitConfigurer classesShouldNotBeAnnotatedWithDisabled() {
    return classesShouldNotBeAnnotatedWithDisabled(CONFIGURATION);
  }

  public JUnitConfigurer classesShouldBePackagePrivate(String regex) {
    return classesShouldBePackagePrivate(regex, CONFIGURATION);
  }

  public JUnitConfigurer classesShouldBePackagePrivate(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().areNotInterfaces().and().haveNameMatching(regex)
            .should().bePackagePrivate()
            .as("Classes with names matching %s should be package-private".formatted(regex)),
        configuration));
  }


  public JUnitConfigurer classesShouldNotBeAnnotatedWithDisabled(Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
            .should().beMetaAnnotatedWith(ANNOTATION_DISABLED)
            .as("Classes should not be annotated with %s".formatted(ANNOTATION_DISABLED)),
        configuration));
  }

  public static final class Disableable extends JUnitConfigurer implements DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public JUnitConfigurer disable() {
      disable(JUnitConfigurer.class);

      return this;
    }
  }
}