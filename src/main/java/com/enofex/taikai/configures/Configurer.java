package com.enofex.taikai.configures;

import com.enofex.taikai.TaikaiRule;
import java.util.Collection;

public interface Configurer {

  default void disable() {
    rules().clear();
  }

  Collection<TaikaiRule> rules();
}
