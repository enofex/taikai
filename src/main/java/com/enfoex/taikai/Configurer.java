package com.enfoex.taikai;

import java.util.Collection;

public interface Configurer {

  default void disable() {
    rules().clear();
  }

  Collection<TaikaiRule> rules();
}
