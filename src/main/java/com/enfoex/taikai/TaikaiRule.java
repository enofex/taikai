package com.enfoex.taikai;

import com.enfoex.taikai.Namespace.IMPORT;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;

public final class TaikaiRule {

  private final ArchRule archRule;

  private String namespace;
  private Namespace.IMPORT namespaceImport;
  private JavaClasses javaClasses;

  private TaikaiRule(ArchRule archRule) {
    this(archRule, null, Namespace.IMPORT.WITHOUT_TESTS);
  }

  private TaikaiRule(ArchRule archRule, String namespace) {
    this(archRule, namespace, Namespace.IMPORT.WITHOUT_TESTS);
  }

  private TaikaiRule(ArchRule archRule, String namespace, Namespace.IMPORT namespaceImport) {
    this(archRule, namespace, namespaceImport, null);
  }

  private TaikaiRule(ArchRule archRule, String namespace, Namespace.IMPORT namespaceImport,
      JavaClasses javaClasses) {
    this.archRule = archRule;
    this.namespace = namespace;
    this.namespaceImport = namespaceImport;
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
    return new TaikaiRule(archRule);
  }

  public static TaikaiRule of(ArchRule archRule, String namespace) {
    return new TaikaiRule(archRule, namespace);
  }

  public static TaikaiRule of(ArchRule archRule, String namespace,
      Namespace.IMPORT namespaceImport) {
    return new TaikaiRule(archRule, namespace, namespaceImport, null);
  }

  public static TaikaiRule of(ArchRule archRule, JavaClasses javaClasses) {
    return new TaikaiRule(archRule, null, null, javaClasses);
  }

  public void check(String globalNamespace) {
    if (this.javaClasses != null) {
      this.archRule.check(this.javaClasses);
    } else if (this.namespaceImport != null) {
      String namespaceToCheck = this.namespace != null ? this.namespace : globalNamespace;

      this.archRule.check(
          this.namespaceImport == IMPORT.WITHOUT_TESTS
              ? Namespace.withoutTests(namespaceToCheck)
              : Namespace.withTests(namespaceToCheck));
    } else {
      throw new IllegalArgumentException(
          "No global namespace and namespace import type or no java classes provided");
    }
  }
}
