package com.enofex.taikai.exports;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.core.TaikaiException;
import com.enofex.taikai.core.TaikaiRule;
import com.enofex.taikai.exports.spi.ExportContext;
import com.enofex.taikai.exports.spi.TaikaiExport;
import com.enofex.taikai.internal.Services;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.EvaluationResult;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * Configures how the {@link TaikaiExport}s discovered with
 * {@code Taikai.Builder.discoverExports(Consumer)} are run.
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

  private final Collection<Class<?>> excludedExports;
  private final Map<Class<?>, Object> configurations;

  /**
   * Creates a configurer that runs all discovered exports without configuration.
   */
  public ExportsConfigurer() {
    this.excludedExports = new ArrayList<>();
    this.configurations = new LinkedHashMap<>();
  }

  /**
   * Creates a copy of the given configurer, so that changing one does not change the other.
   *
   * @param configurer the configurer to copy
   */
  public ExportsConfigurer(ExportsConfigurer configurer) {
    this.excludedExports = new ArrayList<>(configurer.excludedExports);
    this.configurations = new LinkedHashMap<>(configurer.configurations);
  }

  /**
   * Excludes the discovered exports of the given classes. An excluded export is not
   * instantiated. {@code Taikai.Builder.build()} fails if an export is not
   * registered.
   *
   * @param exports the classes of the exports to exclude
   * @return this configurer for fluent chaining
   */
  @SafeVarargs
  public final ExportsConfigurer exclude(Class<? extends TaikaiExport>... exports) {
    this.excludedExports.addAll(List.of(exports));
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
   * <p>{@code Taikai.Builder.build()} fails if the export is not registered or
   * is excluded.</p>
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

  /**
   * Discovers the registered exports, except the excluded ones, each with its context.
   * {@code Taikai.Builder.build()} calls it, so it is rarely needed elsewhere.
   *
   * @param projectDirectory the directory of the project
   * @param namespace the namespace of the builder, or {@code null}
   * @param classes supplies the classes of the builder, without the excluded ones
   * @param rules the configured rules
   * @param evaluate evaluates a rule as the builder checks it
   * @return the exports, each with its context, sorted by their description
   * @throws TaikaiException if the exports cannot be discovered, if an excluded or configured
   *     export is not registered or if a configured export is excluded
   */
  public List<Map.Entry<TaikaiExport, ExportContext>> discover(Path projectDirectory,
      @Nullable String namespace, Supplier<JavaClasses> classes, Collection<TaikaiRule> rules,
      Function<TaikaiRule, EvaluationResult> evaluate) {
    requireNonNull(projectDirectory);
    requireNonNull(classes);
    requireNonNull(rules);
    requireNonNull(evaluate);

    List<TaikaiExport> exports = Services.discover(TaikaiExport.class, "export",
        this.excludedExports, this.configurations.keySet(), TaikaiExport::description,
        (message, cause) -> cause == null
            ? new TaikaiException(message)
            : new TaikaiException(message, cause));
    List<TaikaiRule> configuredRules = List.copyOf(rules);

    return exports.stream()
        .<Map.Entry<TaikaiExport, ExportContext>>map(export -> Map.entry(export,
            new PerExportContext(projectDirectory, export.getClass(),
                this.configurations.get(export.getClass()), namespace, classes, configuredRules,
                evaluate)))
        .toList();
  }}
