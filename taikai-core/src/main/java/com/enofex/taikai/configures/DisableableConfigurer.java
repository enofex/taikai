package com.enofex.taikai.configures;

/**
 * A configurer whose rules can be disabled, for example those that a shared profile adds.
 */
public interface DisableableConfigurer extends Configurer {

  /**
   * Removes the rules added so far to this configurer and to the configurers that it groups.
   *
   * @param <T> the type of this configurer
   * @return this configurer for fluent chaining
   */
  <T extends Configurer> T disable();

}
