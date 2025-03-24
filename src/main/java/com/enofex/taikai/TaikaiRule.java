package com.enofex.taikai;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TaikaiRule {

  private final ArchRule archRule;
  private final Configuration configuration;

  private TaikaiRule(ArchRule archRule, Configuration configuration) {
    this.archRule = requireNonNull(archRule);
    this.configuration = requireNonNullElse(configuration, defaultConfiguration());
  }

  public ArchRule archRule() {
    return this.archRule;
  }

  public Configuration configuration() {
    return this.configuration;
  }

  public static TaikaiRule of(ArchRule archRule) {
    return new TaikaiRule(archRule, defaultConfiguration());
  }

  public static TaikaiRule of(ArchRule archRule, Configuration configuration) {
    return new TaikaiRule(archRule, configuration);
  }

  public void check(String globalNamespace) {
    check(globalNamespace, null, emptySet());
  }

  public void check(String globalNamespace, JavaClasses classes,
      Collection<String> excludedClasses) {
    this.archRule.check(javaClasses(globalNamespace, classes, excludedClasses));
  }

  private JavaClasses javaClasses(String globalNamespace, JavaClasses classes,
      Collection<String> excludedClasses) {
    if (this.configuration.javaClasses() != null) {
      return this.configuration.javaClasses();
    }

    if (classes != null) {
      return classes;
    }

    String namespace = this.configuration.namespace() != null
        ? this.configuration.namespace()
        : globalNamespace;

    if (namespace == null) {
      throw new TaikaiException("Namespace is not provided");
    }

    Collection<String> allExcludedClasses = allExcludedClasses(excludedClasses);
    JavaClasses javaClasses = Namespace.from(namespace, this.configuration.namespaceImport);

    return allExcludedClasses.isEmpty()
        ? javaClasses
        : javaClasses.that(new ExcludeJavaClassDescribedPredicate(allExcludedClasses));
  }

  private Collection<String> allExcludedClasses(Collection<String> excludedClasses) {
    return Stream.concat(
        this.configuration.excludedClasses != null
            ? this.configuration.excludedClasses.stream() : Stream.empty(),
        excludedClasses != null
            ? excludedClasses.stream() : Stream.empty()
    ).toList();
  }

  public static final class Configuration {

    private final String namespace;
    private final Namespace.IMPORT namespaceImport;
    private final JavaClasses javaClasses;
    private final Collection<String> excludedClasses;

    private Configuration(String namespace, Namespace.IMPORT namespaceImport,
        JavaClasses javaClasses, Collection<?> excludedClasses) {
      this.namespace = namespace;
      this.namespaceImport = requireNonNullElse(namespaceImport, Namespace.IMPORT.WITHOUT_TESTS);
      this.javaClasses = javaClasses;
      this.excludedClasses = excludedClasses != null ? toClassNames(excludedClasses) : emptyList();
    }

    private static <T> Collection<String> toClassNames(Collection<T> excludedClasses) {
      if (excludedClasses.isEmpty()) {
        return emptyList();
      }

      T firstElement = excludedClasses.iterator().next();

      if (firstElement instanceof String) {
        return new ArrayList<>((Collection<String>) excludedClasses);
      } else if (firstElement instanceof Class<?>) {
        return excludedClasses.stream()
            .map(clazz -> ((Class<?>) clazz).getName())
            .collect(Collectors.toList());
      } else {
        throw new IllegalArgumentException(
            "Unsupported collection type, only String and Class<?> are supported");
      }
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

    public Collection<String> excludedClasses() {
      return this.excludedClasses;
    }

    public static Configuration defaultConfiguration() {
      return new Configuration(null, Namespace.IMPORT.WITHOUT_TESTS, null, null);
    }

    public static Configuration of(String namespace) {
      return new Configuration(namespace, Namespace.IMPORT.WITHOUT_TESTS, null, null);
    }

    public static <T> Configuration of(String namespace, Collection<T> excludedClasses) {
      return new Configuration(namespace, Namespace.IMPORT.WITHOUT_TESTS, null, excludedClasses);
    }

    public static Configuration of(Namespace.IMPORT namespaceImport) {
      return new Configuration(null, namespaceImport, null, null);
    }

    public static Configuration of(Namespace.IMPORT namespaceImport,
        Collection<String> excludedClasses) {
      return new Configuration(null, namespaceImport, null, excludedClasses);
    }

    public static Configuration of(String namespace, Namespace.IMPORT namespaceImport) {
      return new Configuration(namespace, namespaceImport, null, null);
    }

    public static <T> Configuration of(String namespace, Namespace.IMPORT namespaceImport,
        Collection<T> excludedClasses) {
      return new Configuration(namespace, namespaceImport, null, excludedClasses);
    }

    public static Configuration of(JavaClasses javaClasses) {
      return new Configuration(null, null, javaClasses, null);
    }

    public static <T> Configuration of(JavaClasses javaClasses, Collection<T> excludedClasses) {
      return new Configuration(null, null, javaClasses, excludedClasses);
    }

    public static <T> Configuration of(Collection<T> excludedClasses) {
      return new Configuration(null, null, null, excludedClasses);
    }
  }

  private static final class ExcludeJavaClassDescribedPredicate extends
      DescribedPredicate<JavaClass> {

    private final Collection<String> allExcludedClasses;

    ExcludeJavaClassDescribedPredicate(Collection<String> allExcludedClasses) {
      super("exclude classes");
      this.allExcludedClasses = allExcludedClasses;
    }

    @Override
    public boolean test(JavaClass javaClass) {
      return !this.allExcludedClasses.contains(javaClass.getFullName());
    }
  }
}
