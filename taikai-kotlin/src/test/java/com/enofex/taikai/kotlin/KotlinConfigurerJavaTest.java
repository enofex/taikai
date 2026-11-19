package com.enofex.taikai.kotlin;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.core.TaikaiRule;
import com.enofex.taikai.kotlin.fixtures.GoodDto;
import com.enofex.taikai.kotlin.fixtures.Plain;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import kotlin.Metadata;
import org.junit.jupiter.api.Test;

class KotlinConfigurerJavaTest {

  @Test
  void shouldConfigureKotlinRulesFromJava() {
    Taikai taikai = Taikai.builder().classes(GoodDto.class)
        .configure(kotlin -> kotlin.classesShouldBeDataClasses(".*Dto")
            .dataClassesShouldBeImmutable()
            .classesShouldNotMatch(".*Impl")
            .functionsShouldNotMatch("^(foo|bar)$")
            .propertiesShouldNotMatch(".*(List|Set|Map)$")
            .propertiesShouldMatch(String.class, "name")
            .noUsageOfDeprecatedAPIs(), KotlinConfigurer::new)
        .build();

    assertDoesNotThrow(taikai::checkAll);
  }

  @Test
  void shouldPreserveCrossLanguageDependencies() {
    var classes = new ClassFileImporter().importClasses(JavaClient.class, Plain.class);
    var rule = noClasses().that().haveSimpleName("JavaClient")
        .should().dependOnClassesThat().haveSimpleName("Plain");
    Taikai taikai = Taikai.builder().classes(classes).addRule(TaikaiRule.of(rule)).build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::checkAll);
    assertTrue(failure.getMessage().contains("Plain"));
  }

  @Test
  void shouldReportUnsupportedMetadataInsteadOfPassingSilently() {
    Taikai taikai = Taikai.builder().classes(BrokenMetadata.class)
        .configure(kotlin -> kotlin.classesShouldMatch(".*"), KotlinConfigurer::new)
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::checkAll);
    assertTrue(failure.getMessage().contains("Cannot read Kotlin metadata"));
    assertTrue(failure.getMessage().contains("BrokenMetadata"));
  }

  static class JavaClient {
    private Plain value;
  }

  @Metadata(k = 1, mv = {999, 0, 0})
  static class BrokenMetadata {
  }
}
