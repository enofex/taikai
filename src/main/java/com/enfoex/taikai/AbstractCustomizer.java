package com.enfoex.taikai;

import com.tngtech.archunit.lang.ArchRule;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCustomizer<T> implements Customizer<T> {

  private final List<ArchRule> rules;

  protected AbstractCustomizer() {
    this.rules = new ArrayList<>();
  }

  public boolean enable(ArchRule... rules) {
    return this.rules.addAll(List.of(rules));
  }

  public boolean disable(ArchRule... rules) {
    return this.rules.removeAll(List.of(rules));
  }
}
