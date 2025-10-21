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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Represents a single executable Taikai architectural rule wrapping an ArchUnit {@link ArchRule}.
 *
 * <p>A {@code TaikaiRule} provides optional configuration via
 * {@link Configuration}, allowing customization of namespaces, import options, or class exclusions.</p>
 *
 * <p>Instances can be created via {@link #of(ArchRule)} or {@link #of(ArchRule, Configuration)}.</p>
 *
 * @see ArchRule
 * @see Namespace
 * @see Taikai
 */
public final class TaikaiRule {

  private final ArchRule archRule;
  private final Configuration configuration;

  private TaikaiRule(ArchRule archRule, @Nullable Configuration configuration) {
    this.archRule = requireNonNull(archRule);
    this.configuration = requireNonNullElse(configuration, defaultConfiguration());
  }

  /**
   * Returns the underlying ArchUnit {@link ArchRule}.
   *
   * @return the wrapped ArchRule
   */
  public ArchRule archRule() {
    return this.archRule;
  }

  /**
   * Returns the {@link Configuration} associated with this rule.
   *
   * @return the rule configuration
   */
  public Configuration configuration() {
    return this.configuration;
  }

  /**
   * Creates a {@link TaikaiRule} from a given {@link ArchRule} with the default configuration.
   *
   * @param archRule the ArchUnit rule to wrap
   * @return a new TaikaiRule instance
   */
  public static TaikaiRule of(ArchRule archRule) {
    return new TaikaiRule(archRule, defaultConfiguration());
  }

  /**
   * Creates a {@link TaikaiRule} with a custom {@link Configuration}.
   *
   * @param archRule the ArchUnit rule to wrap
   * @param configuration the rule configuration
   * @return a new TaikaiRule instance
   */
  public static TaikaiRule of(ArchRule archRule, @Nullable Configuration configuration) {
    return new TaikaiRule(archRule, configuration);
  }

  /**
   * Executes this rule for the given namespace.
   *
   * @param globalNamespace the namespace to analyze
   */
  public void check(String globalNamespace) {
    check(globalNamespace, null, emptySet());
  }

  /**
   * Executes this rule for the given namespace or set of classes, excluding specific class names.
   *
   * @param globalNamespace the global namespace (used if no explicit namespace is configured)
   * @param classes optional pre-imported Java classes to analyze
   * @param excludedClasses optional list of fully qualified class names to exclude
   */
  public void check(@Nullable String globalNamespace, @Nullable JavaClasses classes,
      Collection<String> excludedClasses) {
    this.archRule.check(javaClasses(globalNamespace, classes, excludedClasses));
  }

  JavaClasses javaClasses(@Nullable String globalNamespace, @Nullable JavaClasses classes,
      @Nullable Collection<String> excludedClasses) {
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

  private Collection<String> allExcludedClasses(@Nullable Collection<String> excludedClasses) {
    return Stream.concat(
        this.configuration.excludedClasses.stream(),
        excludedClasses != null
            ? excludedClasses.stream() : Stream.empty()
    ).toList();
  }

  public static final class Configuration {

    private final @Nullable String namespace;
    private final Namespace.IMPORT namespaceImport;
    private final @Nullable JavaClasses javaClasses;
    private final Collection<String> excludedClasses;

    private Configuration(@Nullable String namespace, Namespace.@Nullable IMPORT namespaceImport,
        @Nullable JavaClasses javaClasses, @Nullable Collection<?> excludedClasses) {
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

    public @Nullable String namespace() {
      return this.namespace;
    }

    public Namespace.IMPORT namespaceImport() {
      return this.namespaceImport;
    }

    public @Nullable JavaClasses javaClasses() {
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

    private final Collection<Pattern> allExcludedClassPatterns;

    ExcludeJavaClassDescribedPredicate(Collection<String> allExcludedClasses) {
      super("exclude classes");
      this.allExcludedClassPatterns = allExcludedClasses.stream()
          .map(Pattern::quote)
          .map(Pattern::compile)
          .toList();
    }

    @Override
    public boolean test(JavaClass javaClass) {
      return this.allExcludedClassPatterns.stream()
          .noneMatch(pattern -> pattern.matcher(javaClass.getFullName()).matches());
    }
  }
}
