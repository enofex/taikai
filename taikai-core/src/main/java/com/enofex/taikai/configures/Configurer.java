package com.enofex.taikai.configures;

import com.enofex.taikai.TaikaiRule;
import java.util.Collection;

/**
 * A group of rules, such as the rules for Spring controllers.
 */
public interface Configurer {

  /**
   * Removes all rules of this configurer.
   */
  default void clear() {
    rules().clear();
  }

  /**
   * Returns the rules of this configurer, which {@link #clear()} removes.
   *
   * @return the rules
   */
  Collection<TaikaiRule> rules();
}
