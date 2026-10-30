package com.enofex.taikai;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.exports.spi.ExportContext;
import com.enofex.taikai.exports.spi.TaikaiExport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Configures how the {@link TaikaiExport}s discovered with
 * {@link Taikai.Builder#discoverExports(java.util.function.Consumer)} are run.
 *
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .discoverExports(exports -> exports
 *         .exclude(RuleCountExport.class)
 *         .configure(AgentsMarkdownExport.class, AgentsMarkdownExport.file("ARCHITECTURE.md")))
 *     .build()
 *     .export();
 * }</pre>
 *
 * @see TaikaiExport
 */
public final class ExportsConfigurer {

  static final String REGISTRATION_FILE = "META-INF/services/" + TaikaiExport.class.getName();

  private final Collection<Class<?>> excludedExports;
  private final Map<Class<?>, Object> configurations;

  ExportsConfigurer() {
    this.excludedExports = new ArrayList<>();
    this.configurations = new LinkedHashMap<>();
  }

  ExportsConfigurer(ExportsConfigurer configurer) {
    this.excludedExports = new ArrayList<>(configurer.excludedExports);
    this.configurations = new LinkedHashMap<>(configurer.configurations);
  }

  /**
   * Excludes the discovered exports of the given classes. An excluded export is not
   * instantiated. {@link Taikai.Builder#build()} fails if an export is not registered.
   *
   * @param exports the classes of the exports to exclude
   * @return this configurer for fluent chaining
   */
  @SafeVarargs
  public final ExportsConfigurer exclude(Class<? extends TaikaiExport>... exports) {
    this.excludedExports.addAll(Arrays.asList(exports));
    return this;
  }

  /**
   * Configures an export, for example with its settings. The export gets the configuration with
   * {@link ExportContext#configuration(Class)}; other exports do not see it. Configuring an export
   * again replaces its configuration.
   *
   * <pre>{@code
   * .configure(AgentsMarkdownExport.class, AgentsMarkdownExport.file("ARCHITECTURE.md"))
   * }</pre>
   *
   * <p>{@link Taikai.Builder#build()} fails if the export is not registered or is excluded.</p>
   *
   * @param export the class of the export
   * @param configuration the configuration of the export
   * @param <E> the type of the export
   * @return this configurer for fluent chaining
   */
  public <E extends TaikaiExport> ExportsConfigurer configure(Class<E> export,
      Object configuration) {
    this.configurations.put(requireNonNull(export), requireNonNull(configuration));
    return this;
  }

  Collection<Class<?>> excludedExports() {
    return List.copyOf(this.excludedExports);
  }

  void validate(Collection<? extends Class<?>> registeredExports) {
    for (Class<?> excludedExport : this.excludedExports) {
      if (!registeredExports.contains(excludedExport)) {
        throw new TaikaiException("Export %s is excluded, but not registered in %s"
            .formatted(excludedExport.getName(), REGISTRATION_FILE));
      }
    }

    for (Class<?> configuredExport : this.configurations.keySet()) {
      if (this.excludedExports.contains(configuredExport)) {
        throw new TaikaiException("Export %s is configured, but excluded"
            .formatted(configuredExport.getName()));
      }

      if (!registeredExports.contains(configuredExport)) {
        throw new TaikaiException("Export %s is configured, but not registered in %s"
            .formatted(configuredExport.getName(), REGISTRATION_FILE));
      }
    }
  }

  @Nullable Object configuration(Class<?> export) {
    return this.configurations.get(export);
  }
}
