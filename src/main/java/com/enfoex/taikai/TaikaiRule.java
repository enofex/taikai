package com.enfoex.taikai;

import com.enfoex.taikai.Namespace.IMPORT;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Objects;

public final class TaikaiRule {

  private final ArchRule archRule;
  private final Configuration configuration;

  private TaikaiRule(ArchRule archRule, Configuration configuration) {
    this.archRule = Objects.requireNonNull(archRule);
    this.configuration = configuration;
  }

  public ArchRule archRule() {
    return this.archRule;
  }

  public Configuration configuration() {
    return this.configuration;
  }

  public static TaikaiRule of(ArchRule archRule) {
    return new TaikaiRule(archRule, Configuration.of());
  }

  public static TaikaiRule of(ArchRule archRule, Configuration configuration) {
    return new TaikaiRule(archRule, configuration);
  }

  public void check(String globalNamespace) {
    if (this.configuration.javaClasses() != null) {
      this.archRule.check(this.configuration.javaClasses());
    } else {
      String namespaceToCheck = this.configuration.namespace() != null
          ? this.configuration.namespace() : globalNamespace;

      if (namespaceToCheck == null) {
        throw new IllegalArgumentException(
            "No global namespace and no specific namespace provided.");
      }

      this.archRule.check(
          this.configuration.namespaceImport() == IMPORT.WITHOUT_TESTS
              ? Namespace.withoutTests(namespaceToCheck)
              : Namespace.withTests(namespaceToCheck));
    }
  }

  public static final class Configuration {

    private final String namespace;
    private final Namespace.IMPORT namespaceImport;
    private final JavaClasses javaClasses;

    private Configuration(String namespace, Namespace.IMPORT namespaceImport,
        JavaClasses javaClasses) {
      this.namespace = namespace;
      this.namespaceImport = namespaceImport != null
          ? namespaceImport : Namespace.IMPORT.WITHOUT_TESTS;
      this.javaClasses = javaClasses;
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

    public static Configuration of() {
      return new Configuration(null, Namespace.IMPORT.WITHOUT_TESTS, null);
    }

    public static Configuration of(String namespace) {
      return new Configuration(namespace, Namespace.IMPORT.WITHOUT_TESTS, null);
    }

    public static Configuration of(String namespace, Namespace.IMPORT namespaceImport) {
      return new Configuration(namespace, namespaceImport, null);
    }

    public static Configuration of(JavaClasses javaClasses) {
      return new Configuration(null, null, javaClasses);
    }
  }
}
