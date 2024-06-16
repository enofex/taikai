package com.enofex.taikai.test;

import static com.enofex.taikai.test.JUnit5Predicates.ANNOTATION_PARAMETRIZED_TEST;
import static com.enofex.taikai.test.JUnit5Predicates.ANNOTATION_TEST;
import static com.enofex.taikai.test.JUnit5Predicates.annotatedWithTestOrParameterizedTest;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.enofex.taikai.Namespace;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;

public final class JUnit5Configurer extends AbstractConfigurer {

  private static final String ANNOTATION_DISABLED = "org.junit.jupiter.api.Disabled";

  JUnit5Configurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public JUnit5Configurer methodsShouldBePackagePrivate() {
    return methodsShouldBePackagePrivate(Configuration.of(Namespace.IMPORT.WITH_TESTS));
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
    return methodsShouldNotBeAnnotatedWithDisabled(Configuration.of(Namespace.IMPORT.WITH_TESTS));
  }

  public JUnit5Configurer methodsShouldNotBeAnnotatedWithDisabled(Configuration configuration) {
    return addRule(TaikaiRule.of(noMethods()
            .should().beMetaAnnotatedWith(ANNOTATION_DISABLED)
            .as("Methods should not be annotated with %s".formatted(ANNOTATION_DISABLED)),
        configuration));
  }

  public JUnit5Configurer classesShouldNotBeAnnotatedWithDisabled() {
    return classesShouldNotBeAnnotatedWithDisabled(Configuration.of(Namespace.IMPORT.WITH_TESTS));
  }

  public JUnit5Configurer classesShouldNotBeAnnotatedWithDisabled(Configuration configuration) {
    return addRule(TaikaiRule.of(noClasses()
            .should().beMetaAnnotatedWith(ANNOTATION_DISABLED)
            .as("Classes should not be annotated with %s".formatted(ANNOTATION_DISABLED)),
        configuration));
  }
}