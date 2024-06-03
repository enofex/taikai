package com.enfoex.taikai;

import com.enfoex.taikai.Namespace.IMPORT;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;

public final class TaikaiRule {

  private final ArchRule archRule;
  private final String namespace;
  private final Namespace.IMPORT namespaceImport;
  private final JavaClasses javaClasses;

  private TaikaiRule(ArchRule archRule, String namespace, Namespace.IMPORT namespaceImport, JavaClasses javaClasses) {
    this.archRule = archRule;
    this.namespace = namespace;
    this.namespaceImport = namespaceImport != null ? namespaceImport : Namespace.IMPORT.WITHOUT_TESTS;
    this.javaClasses = javaClasses;
  }

  public ArchRule archRule() {
    return this.archRule;
  }

  public String namespace() {
    return this.namespace;
  }

  public Namespace.IMPORT namespaceImport() {
    return this.namespaceImport;
  }

  public JavaClasses javaClasses() {
    return this.javaClasses;
  }

  public static TaikaiRule of(ArchRule archRule) {
    return new TaikaiRule(archRule, null, Namespace.IMPORT.WITHOUT_TESTS, null);
  }

  public static TaikaiRule of(ArchRule archRule, String namespace) {
    return new TaikaiRule(archRule, namespace, Namespace.IMPORT.WITHOUT_TESTS, null);
  }

  public static TaikaiRule of(ArchRule archRule, String namespace, Namespace.IMPORT namespaceImport) {
    return new TaikaiRule(archRule, namespace, namespaceImport, null);
  }

  public static TaikaiRule of(ArchRule archRule, JavaClasses javaClasses) {
    return new TaikaiRule(archRule, null, null, javaClasses);
  }

  public void check(String globalNamespace) {
    if (this.javaClasses != null) {
      this.archRule.check(this.javaClasses);
    } else {
      String namespaceToCheck = this.namespace != null ? this.namespace : globalNamespace;

      if (namespaceToCheck == null) {
        throw new IllegalArgumentException("No global namespace and no specific namespace provided.");
      }

      this.archRule.check(
          this.namespaceImport == IMPORT.WITHOUT_TESTS
              ? Namespace.withoutTests(namespaceToCheck)
              : Namespace.withTests(namespaceToCheck));
    }
  }
}
