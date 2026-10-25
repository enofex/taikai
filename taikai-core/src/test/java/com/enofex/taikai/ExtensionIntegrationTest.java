package com.enofex.taikai;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExtensionIntegrationTest {
  @Test
  void shouldAccumulateRulesFromAnExtensionConfigurer() {
    Taikai taikai = Taikai.builder().classes(ExtensionIntegrationTest.class)
        .configure(extension -> extension.addExampleRule(), ExampleConfigurer::new)
        .configure(extension -> extension.addExampleRule(), ExampleConfigurer::new)
        .build();

    assertEquals(2, taikai.rules().size());
    taikai.checkAll();
  }

  @Test
  void shouldCopyRulesAndExclusionsWhenCreatingABuilder() {
    Taikai original = Taikai.builder().classes(ExtensionIntegrationTest.class)
        .excludeClasses("com.example.First")
        .configure(extension -> extension.addExampleRule(), ExampleConfigurer::new)
        .build();
    Taikai extended = original.toBuilder().excludeClasses("com.example.Second")
        .addRule(TaikaiRule.of(classes().should().bePackagePrivate())).build();

    assertEquals(1, original.rules().size());
    assertEquals(1, original.excludedClasses().size());
    assertEquals(2, extended.rules().size());
    assertEquals(2, extended.excludedClasses().size());
    extended.checkAll();
  }

  @Test
  void shouldApplyExclusionsToExplicitlyImportedClasses() {
    var imported = new ClassFileImporter().importClasses(ExtensionIntegrationTest.class);
    var failingRule = classes().should().bePublic();
    assertThrows(AssertionError.class, () -> failingRule.check(imported));

    assertDoesNotThrow(() -> Taikai.builder()
        .addRule(TaikaiRule.of(failingRule,
            Configuration.of(imported, List.of(ExtensionIntegrationTest.class.getName()))))
        .build().check());
  }

  private static class ExampleConfigurer extends AbstractConfigurer {
    ExampleConfigurer(ConfigurerContext context) {
      super(context);
    }

    ExampleConfigurer addExampleRule() {
      return addRule(TaikaiRule.of(classes().should().bePackagePrivate()));
    }
  }
}
