package com.enofex.taikai;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.core.TaikaiRule;
import org.junit.jupiter.api.Test;

class TaikaiExtensionsTest {

  @Test
  void shouldAccumulateRulesFromAnExtensionConfigurer() {
    Taikai taikai = Taikai.builder()
        .classes(TaikaiExtensionsTest.class)
        .configure(ExampleConfigurer::addExampleRule, ExampleConfigurer::new)
        .configure(ExampleConfigurer::addExampleRule, ExampleConfigurer::new)
        .build();

    assertEquals(2, taikai.rules().size());
    assertDoesNotThrow(taikai::checkAll);
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
