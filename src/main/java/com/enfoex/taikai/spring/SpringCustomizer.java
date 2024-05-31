package com.enfoex.taikai.spring;

import com.enfoex.taikai.Customizer;
import com.enfoex.taikai.Taikai.TaikaiBuilder;
import com.tngtech.archunit.lang.ArchRule;
import java.util.ArrayList;
import java.util.List;

public final class SpringCustomizer implements Customizer<TaikaiBuilder> {

  private final List<ArchRule> rules;

  public SpringCustomizer() {
    this.rules = new ArrayList<>();
  }

  public void enable(ArchRule... rules) {
    this.rules.addAll(List.of(rules));
  }

  @Override
  public void customize(TaikaiBuilder builder) {

  }
}
