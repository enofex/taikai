package com.enofex.taikai;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TaikaiRule {

  private final ArchRule archRule;
  private final Configuration configuration;

  private TaikaiRule(ArchRule archRule, Configuration configuration) {
    this.archRule = requireNonNull(archRule);
    this.configuration = requireNonNullElse(configuration, Configuration.defaultConfiguration());
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
    check(globalNamespace, null, Collections.emptySet());
  }

  public void check(String globalNamespace, JavaClasses classes, Set<String> excludedClasses) {
    if (this.configuration.javaClasses() != null) {
      this.archRule.check(this.configuration.javaClasses());
    } else if (classes != null) {
      this.archRule.check(classes);
    } else {
      String namespace = this.configuration.namespace() != null
          ? this.configuration.namespace()
          : globalNamespace;

      if (namespace == null) {
        throw new TaikaiException("Namespace is not provided");
      }

      Set<String> allExcludedClasses = allExcludedClasses(excludedClasses);

      if (allExcludedClasses.isEmpty()) {
        this.archRule.check(Namespace.from(namespace, this.configuration.namespaceImport));
      } else {
        this.archRule.check(Namespace.from(namespace, this.configuration.namespaceImport)
            .that(new DescribedPredicate<>("exclude classes") {
              @Override
              public boolean test(JavaClass javaClass) {
                return !allExcludedClasses.contains(javaClass.getFullName());
              }
            }));
      }
    }
  }

  private Set<String> allExcludedClasses(Set<String> excludedClasses) {
    return Stream.concat(
        this.configuration.excludedClasses != null
            ? this.configuration.excludedClasses.stream() : Stream.empty(),
        excludedClasses != null
            ? excludedClasses.stream() : Stream.empty())
        .collect(Collectors.toSet());
  }

  public static final class Configuration {

    private final String namespace;
    private final Namespace.IMPORT namespaceImport;
    private final JavaClasses javaClasses;
    private final Set<String> excludedClasses;

    private Configuration(String namespace, Namespace.IMPORT namespaceImport,
        JavaClasses javaClasses, Set<String> excludedClasses) {
      this.namespace = namespace;
      this.namespaceImport = requireNonNullElse(namespaceImport, Namespace.IMPORT.WITHOUT_TESTS);
      this.javaClasses = javaClasses;
      this.excludedClasses = excludedClasses != null ? excludedClasses : Collections.emptySet();
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

    public Set<String> excludedClasses() {
      return this.excludedClasses;
    }

    public static Configuration defaultConfiguration() {
      return new Configuration(null, Namespace.IMPORT.WITHOUT_TESTS, null, null);
    }

    public static Configuration of(String namespace) {
      return new Configuration(namespace, Namespace.IMPORT.WITHOUT_TESTS, null, null);
    }

    public static Configuration of(String namespace, Set<String> excludedClasses) {
      return new Configuration(namespace, Namespace.IMPORT.WITHOUT_TESTS, null, excludedClasses);
    }

    public static Configuration of(Namespace.IMPORT namespaceImport) {
      return new Configuration(null, namespaceImport, null, null);
    }

    public static Configuration of(Namespace.IMPORT namespaceImport, Set<String> excludedClasses) {
      return new Configuration(null, namespaceImport, null, excludedClasses);
    }

    public static Configuration of(String namespace, Namespace.IMPORT namespaceImport) {
      return new Configuration(namespace, namespaceImport, null, null);
    }

    public static Configuration of(String namespace, Namespace.IMPORT namespaceImport,
        Set<String> excludedClasses) {
      return new Configuration(namespace, namespaceImport, null, excludedClasses);
    }

    public static Configuration of(JavaClasses javaClasses) {
      return new Configuration(null, null, javaClasses, null);
    }

    public static Configuration of(JavaClasses javaClasses, Set<String> excludedClasses) {
      return new Configuration(null, null, javaClasses, excludedClasses);
    }

    public static Configuration of(Set<String> excludedClasses) {
      return new Configuration(null, null, null, excludedClasses);
    }
  }
}
