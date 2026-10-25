package com.enofex.taikai;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.java.JavaConfigurer;
import com.enofex.taikai.test.JUnitConfigurer;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigurationWarningsTest {

  private static final String NAMESPACE = "com.enofex.taikai";
  private static final String MISSPELLED_NAMESPACE = "com.enofex.taikia";

  @Test
  void shouldWarnAboutNamespaceWithoutClasses() {
    Taikai taikai = Taikai.builder()
        .namespace(MISSPELLED_NAMESPACE)
        .java(java -> java
            .noUsageOfSystemOutOrErr()
            .fieldsShouldNotBePublic())
        .build();

    assertEquals(List.of("Namespace com.enofex.taikia contains no production classes, so the "
            + "rules for them check nothing. Is the namespace correct?"),
        ConfigurationWarnings.of(taikai));
  }

  @Test
  void shouldWarnAboutNamespaceWithoutClassesPerImportMode() {
    Taikai taikai = Taikai.builder()
        .namespace(MISSPELLED_NAMESPACE)
        .java(JavaConfigurer::noUsageOfSystemOutOrErr)
        .test(test -> test.junit(JUnitConfigurer::classesShouldNotBeAnnotatedWithDisabled))
        .build();

    assertEquals(List.of(
            "Namespace com.enofex.taikia contains no production classes, so the rules for them "
                + "check nothing. Is the namespace correct?",
            "Namespace com.enofex.taikia contains no test classes, so the rules for them check "
                + "nothing. Is the namespace correct?"),
        ConfigurationWarnings.of(taikai));
  }

  @Test
  void shouldNotWarnAboutNamespaceWithClasses() {
    Taikai taikai = Taikai.builder()
        .namespace(NAMESPACE)
        .java(JavaConfigurer::noUsageOfSystemOutOrErr)
        .build();

    assertEquals(List.of(), ConfigurationWarnings.of(taikai));
  }

  @Test
  void shouldNotFailForNamespaceWithoutClasses() {
    Taikai taikai = Taikai.builder()
        .namespace(MISSPELLED_NAMESPACE)
        .java(JavaConfigurer::noUsageOfSystemOutOrErr)
        .build();

    assertDoesNotThrow(taikai::check);
    assertDoesNotThrow(taikai::checkAll);
  }

  @Test
  void shouldWarnAboutUnusedExcludedClasses() {
    Taikai taikai = Taikai.builder()
        .namespace(NAMESPACE)
        .excludeClasses(
            "com.enofex.taikai.Taikai",
            "com.enofex.taikai.java..",
            "com.enofex.taikai.configures.*",
            "com.enofex.taikai.NotExisting",
            "com.enofex.taikai.notexisting.*")
        .java(JavaConfigurer::noUsageOfSystemOutOrErr)
        .build();

    assertEquals(List.of(
            "Exclusion 'com.enofex.taikai.NotExisting' matches no class, so it can be removed",
            "Exclusion 'com.enofex.taikai.notexisting.*' matches no class, so it can be removed"),
        ConfigurationWarnings.of(taikai));
  }

  @Test
  void shouldWarnAboutUnusedExcludedCriteria() {
    DescribedPredicate<JavaClass> unused = ClassCriteria.nameMatching(".*NotExisting");

    Taikai taikai = Taikai.builder()
        .namespace(NAMESPACE)
        .excludeClasses(ClassCriteria.nameMatching(".*Taikai"))
        .excludeClasses(unused)
        .java(JavaConfigurer::noUsageOfSystemOutOrErr)
        .build();

    assertEquals(List.of(
            "Exclusion '%s' matches no class, so it can be removed".formatted(
                unused.getDescription())),
        ConfigurationWarnings.of(taikai));
  }

  @Test
  void shouldWarnAboutUnusedExcludedClassesOfRule() {
    Taikai taikai = Taikai.builder()
        .namespace(NAMESPACE)
        .java(java -> java.noUsageOfSystemOutOrErr(Configuration.of(List.of(
            "com.enofex.taikai.Taikai", "com.enofex.taikai.NotExisting"))))
        .build();

    assertEquals(List.of("Exclusion 'com.enofex.taikai.NotExisting' of rule 'Classes should not "
            + "use System.out or System.err' matches no class, so it can be removed"),
        ConfigurationWarnings.of(taikai));
  }

  @Test
  void shouldWarnAboutUnusedExcludedClassesOfRuleWithClasses() {
    Taikai taikai = Taikai.builder()
        .namespace(NAMESPACE)
        .java(java -> java.noUsageOfSystemOutOrErr(Configuration.of(
            new ClassFileImporter().importClasses(Taikai.class),
            List.of("com.enofex.taikai.NotExisting"))))
        .build();

    assertEquals(List.of("Exclusion 'com.enofex.taikai.NotExisting' of rule 'Classes should not "
            + "use System.out or System.err' matches no class, so it can be removed"),
        ConfigurationWarnings.of(taikai));
  }

  @Test
  void shouldNotWarnAboutExclusionsOfNamespaceWithoutClasses() {
    Taikai taikai = Taikai.builder()
        .namespace(MISSPELLED_NAMESPACE)
        .excludeClasses("com.enofex.taikai.NotExisting")
        .java(JavaConfigurer::noUsageOfSystemOutOrErr)
        .build();

    assertEquals(1, ConfigurationWarnings.of(taikai).size());
  }

  @Test
  void shouldNotWarnWhenClassesAreGiven() {
    Taikai taikai = Taikai.builder()
        .classes(Taikai.class)
        .excludeClasses("com.enofex.taikai.NotExisting")
        .java(JavaConfigurer::noUsageOfSystemOutOrErr)
        .build();

    assertEquals(List.of(), ConfigurationWarnings.of(taikai));
  }
}
