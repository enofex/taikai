package com.enofex.taikai.configures;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.enofex.taikai.TaikaiRule;
import com.tngtech.archunit.lang.ArchRule;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.Test;

class ConfigurerTest {

  @Test
  void shouldClearRules() {
    ArchRule archRule = mock(ArchRule.class);
    TaikaiRule taikaiRule = TaikaiRule.of(archRule);

    TestConfigurer configurer = new TestConfigurer();
    configurer.rules().add(taikaiRule);

    configurer.clear();

    assertTrue(configurer.rules().isEmpty());
  }


  private static final class TestConfigurer implements Configurer {

    private final Collection<TaikaiRule> rules = new ArrayList<>();

    @Override
    public Collection<TaikaiRule> rules() {
      return this.rules;
    }
  }
}
