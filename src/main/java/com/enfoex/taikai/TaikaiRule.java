package com.enfoex.taikai;

import com.enfoex.taikai.Namespace.IMPORT;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;

public final class TaikaiRule {

  private final ArchRule archUnit;

  private Namespace.IMPORT namespaceImport;
  private JavaClasses javaClasses;

  private TaikaiRule(ArchRule archUnit) {
    this(archUnit, Namespace.IMPORT.WITHOUT_TESTS);
  }

  private TaikaiRule(ArchRule archUnit, Namespace.IMPORT namespaceImport) {
    this(archUnit, namespaceImport, null);
  }

  private TaikaiRule(ArchRule archUnit, Namespace.IMPORT namespaceImport, JavaClasses javaClasses) {
    this.archUnit = archUnit;
    this.namespaceImport = namespaceImport;
    this.javaClasses = javaClasses;
  }

  public ArchRule archUnit() {
    return this.archUnit;
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
      this.archUnit.check(
          this.namespaceImport == IMPORT.WITHOUT_TESTS ? Namespace.withoutTests(namespace)
              : Namespace.withTests(namespace));
    } else if (this.javaClasses != null) {
      this.archUnit.check(this.javaClasses);
    }
  }
}
