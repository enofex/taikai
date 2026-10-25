package com.enofex.taikai.configures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.TaikaiRule;
import java.util.Collection;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class ConfigurersTest {

  private final Configurers configurers = new Configurers();

  @Test
  void shouldThrowNullPointerExceptionWhenGetOrApplyWithNull() {
    assertThrows(NullPointerException.class, () -> this.configurers.getOrApply(null));
  }

  @Test
  void shouldGetOrApplyReturnExistingConfigurer() {
    TestConfigurer testConfigurer = new TestConfigurer();
    this.configurers.getOrApply(testConfigurer);
    TestConfigurer retrievedConfigurer = this.configurers.getOrApply(new TestConfigurer());

    assertSame(testConfigurer, retrievedConfigurer);
  }

  @Test
  void shouldGetOrApplyApplyNewConfigurer() {
    TestConfigurer testConfigurer = new TestConfigurer();
    TestConfigurer retrievedConfigurer = this.configurers.getOrApply(testConfigurer);

    assertSame(testConfigurer, retrievedConfigurer);
    assertEquals(1, this.configurers.all().size());
  }

  @Test
  void shouldGetReturnConfigurerByClass() {
    TestConfigurer testConfigurer = new TestConfigurer();
    this.configurers.getOrApply(testConfigurer);
    TestConfigurer retrievedConfigurer = this.configurers.get(TestConfigurer.class);

    assertSame(testConfigurer, retrievedConfigurer);
  }

  @Test
  void shouldGetReturnNullForUnknownClass() {
    assertNull(this.configurers.get(TestConfigurer.class));
  }

  @Test
  void shouldAllReturnAllConfigurers() {
    TestConfigurer testConfigurer1 = new TestConfigurer();
    AnotherTestConfigurer testConfigurer2 = new AnotherTestConfigurer();
    this.configurers.getOrApply(testConfigurer1);
    this.configurers.getOrApply(testConfigurer2);

    Collection<Configurer> allConfigurers = this.configurers.all();

    assertEquals(2, allConfigurers.size());
    assertTrue(allConfigurers.contains(testConfigurer1));
    assertTrue(allConfigurers.contains(testConfigurer2));
  }

  @Test
  void shouldIteratorIterateOverAllConfigurers() {
    TestConfigurer testConfigurer1 = new TestConfigurer();
    AnotherTestConfigurer testConfigurer2 = new AnotherTestConfigurer();
    this.configurers.getOrApply(testConfigurer1);
    this.configurers.getOrApply(testConfigurer2);

    Iterator<Configurer> iterator = this.configurers.iterator();
    assertTrue(iterator.hasNext());
    assertSame(testConfigurer1, iterator.next());
    assertSame(testConfigurer2, iterator.next());
    assertFalse(iterator.hasNext());
  }

  private static class TestConfigurer implements Configurer {

    @Override
    public Collection<TaikaiRule> rules() {
      return null;
    }
  }

  private static class AnotherTestConfigurer implements Configurer {

    @Override
    public Collection<TaikaiRule> rules() {
      return null;
    }
  }
}
