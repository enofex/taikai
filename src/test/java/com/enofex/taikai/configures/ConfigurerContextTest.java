package com.enofex.taikai.configures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ConfigurerContextTest {

  private static final String VALID_NAMESPACE = "com.example";
  private static final Configurers VALID_CONFIGURERS = new Configurers();

  @Test
  void shouldReturnNamespace() {
    ConfigurerContext context = new ConfigurerContext(VALID_NAMESPACE, VALID_CONFIGURERS);

    assertEquals(VALID_NAMESPACE, context.namespace());
  }

  @Test
  void shouldReturnConfigurers() {
    ConfigurerContext context = new ConfigurerContext(VALID_NAMESPACE, VALID_CONFIGURERS);

    assertEquals(VALID_CONFIGURERS, context.configurers());
  }

  @Test
  void shouldHandleNullNamespace() {
    ConfigurerContext context = new ConfigurerContext(null, VALID_CONFIGURERS);

    assertNull(context.namespace());
  }

  @Test
  void shouldHandleNullConfigurers() {
    ConfigurerContext context = new ConfigurerContext(VALID_NAMESPACE, null);

    assertNull(context.configurers());
  }

  @Test
  void shouldHandleNullParameters() {
    ConfigurerContext context = new ConfigurerContext(null, null);

    assertNull(context.namespace());
    assertNull(context.configurers());
  }
}