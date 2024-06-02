package com.enfoex.taikai;

import com.tngtech.archunit.lang.ArchRule;
import java.util.Collection;


public interface Configurer {

  default void disable() {
    rules().clear();
  }

  Collection<ArchRule> rules();
}
