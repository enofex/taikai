package com.enofex.taikai;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Objects;

public final class TaikaiRule {

  private final ArchRule archRule;
  private final Configuration configuration;

  private TaikaiRule(ArchRule archRule, Configuration configuration) {
    this.archRule = Objects.requireNonNull(archRule);
    this.configuration = Objects.requireNonNullElse(configuration,
        Configuration.defaultConfiguration());
  }

  public ArchRule archRule() {
    return this.archRule;
  }

  public Configuration configuration() {
    return this.configuration;
  }

  public static TaikaiRule of(ArchRule archRule) {
    return new TaikaiRule(archRule, Configuration.defaultConfiguration());
  }

  public static TaikaiRule of(ArchRule archRule, Configuration configuration) {
    return new TaikaiRule(archRule, configuration);
  }

  public void check(String globalNamespace) {
    if (this.configuration.javaClasses() != null) {
      this.archRule.check(this.configuration.javaClasses());
    } else {
      String namespace = this.configuration.namespace() != null
          ? this.configuration.namespace()
          : globalNamespace != null
              ? globalNamespace
              : null;

      if (namespace == null) {
        throw new TaikaiException("Namespace is not provided");
      }

      this.archRule.check(Namespace.from(namespace, this.configuration.namespaceImport));
    }
  }

  public static final class Configuration {

    private final String namespace;
    private final Namespace.IMPORT namespaceImport;
    private final JavaClasses javaClasses;

    private Configuration(String namespace, Namespace.IMPORT namespaceImport,
        JavaClasses javaClasses) {
      this.namespace = namespace;
      this.namespaceImport = Objects.requireNonNullElse(namespaceImport,
          Namespace.IMPORT.WITHOUT_TESTS);
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

    public static Configuration defaultConfiguration() {
      return new Configuration(null, Namespace.IMPORT.WITHOUT_TESTS, null);
    }

    public static Configuration of(String namespace) {
      return new Configuration(namespace, Namespace.IMPORT.WITHOUT_TESTS, null);
    }

    public static Configuration of(Namespace.IMPORT namespaceImport) {
      return new Configuration(null, namespaceImport, null);
    }

    public static Configuration of(String namespace, Namespace.IMPORT namespaceImport) {
      return new Configuration(namespace, namespaceImport, null);
    }

    public static Configuration of(JavaClasses javaClasses) {
      return new Configuration(null, null, javaClasses);
    }
  }
}
