package com.enofex.taikai.configures;

/**
 * Customizes a configurer, usually with a lambda that adds rules to it.
 *
 * @param <T> the type of the configurer
 */
@FunctionalInterface
public interface Customizer<T extends Configurer> {

  /**
   * Customizes the given configurer.
   *
   * @param t the configurer
   */
  void customize(T t);

}
