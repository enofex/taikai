package com.enfoex.taikai;

import com.enfoex.taikai.Namespace.IMPORT;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;

public final class TaikaiRule {

  private final ArchRule archRule;

  private Namespace.IMPORT namespaceImport;
  private JavaClasses javaClasses;

  private TaikaiRule(ArchRule archRule) {
    this(archRule, Namespace.IMPORT.WITHOUT_TESTS);
  }

  private TaikaiRule(ArchRule archRule, Namespace.IMPORT namespaceImport) {
    this(archRule, namespaceImport, null);
  }

  private TaikaiRule(ArchRule archRule, Namespace.IMPORT namespaceImport, JavaClasses javaClasses) {
    this.archRule = archRule;
    this.namespaceImport = namespaceImport;
    this.javaClasses = javaClasses;
  }

  public ArchRule archRule() {
    return this.archRule;
  }

  public Namespace.IMPORT namespaceImport() {
    return this.namespaceImport;
  }

  public JavaClasses javaClasses() {
    return this.javaClasses;
  }

  public static TaikaiRule of(ArchRule archRule) {
    return new TaikaiRule(archRule);
  }

  public static TaikaiRule of(ArchRule archRule, Namespace.IMPORT namespaceImport) {
    return new TaikaiRule(archRule, namespaceImport);
  }

  public static TaikaiRule of(ArchRule archRule, JavaClasses javaClasses) {
    return new TaikaiRule(archRule, null, javaClasses);
  }

  public void check(String namespace) {
    if (namespace != null && this.namespaceImport != null) {
      this.archRule.check(
          this.namespaceImport == IMPORT.WITHOUT_TESTS
              ? Namespace.withoutTests(namespace)
              : Namespace.withTests(namespace));
    } else if (this.javaClasses != null) {
      this.archRule.check(this.javaClasses);
    } else {
      throw new IllegalArgumentException(
          "No namespace and namespace type or no java classes provided");
    }
  }
}
