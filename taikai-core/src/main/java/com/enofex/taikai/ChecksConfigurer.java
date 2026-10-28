package com.enofex.taikai;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.ForwardingCheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import org.jspecify.annotations.Nullable;

/**
 * Configures how the {@link TaikaiCheck}s discovered with
 * {@link Taikai.Builder#discoverChecks(java.util.function.Consumer)} are run.
 *
 * <pre>{@code
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .discoverChecks(checks -> checks
 *         .exclude(LicenseCheck.class)
 *         .configure(ForbiddenDependenciesCheck.class,
 *             new ForbiddenDependencies(List.of("log4j:log4j")))
 *         .context(context -> new ForwardingCheckContext(context) {
 *           @Override
 *           public ClassLoader classLoader() {
 *             return myClassLoader;
 *           }
 *         }))
 *     .build()
 *     .checkAll();
 * }</pre>
 *
 * @see TaikaiCheck
 */
public final class ChecksConfigurer {

  static final String REGISTRATION_FILE = "META-INF/services/" + TaikaiCheck.class.getName();

  private final Collection<Class<?>> excludedChecks;
  private final Map<Class<?>, Object> configurations;
  private final Collection<ContextFunction> contextFunctions;

  ChecksConfigurer() {
    this.excludedChecks = new ArrayList<>();
    this.configurations = new LinkedHashMap<>();
    this.contextFunctions = new ArrayList<>();
  }

  ChecksConfigurer(ChecksConfigurer configurer) {
    this.excludedChecks = new ArrayList<>(configurer.excludedChecks);
    this.configurations = new LinkedHashMap<>(configurer.configurations);
    this.contextFunctions = new ArrayList<>(configurer.contextFunctions);
  }

  /**
   * Excludes the discovered checks of the given classes. An excluded check is not instantiated.
   * {@link Taikai.Builder#build()} fails if a check is not registered.
   *
   * @param checks the classes of the checks to exclude
   * @return this configurer for fluent chaining
   */
  @SafeVarargs
  public final ChecksConfigurer exclude(Class<? extends TaikaiCheck>... checks) {
    this.excludedChecks.addAll(Arrays.asList(checks));
    return this;
  }

  /**
   * Configures a check, for example with its settings. The check gets the configuration with
   * {@link CheckContext#configuration(Class)}; other checks do not see it. Configuring a
   * check again replaces its configuration.
   *
   * <pre>{@code
   * .configure(ForbiddenDependenciesCheck.class,
   *     new ForbiddenDependencies(List.of("log4j:log4j")))
   * }</pre>
   *
   * <p>{@link Taikai.Builder#build()} fails if the check is not registered or is excluded.</p>
   *
   * @param check the class of the check
   * @param configuration the configuration of the check
   * @param <C> the type of the check
   * @return this configurer for fluent chaining
   */
  public <C extends TaikaiCheck> ChecksConfigurer configure(Class<C> check, Object configuration) {
    this.configurations.put(requireNonNull(check), requireNonNull(configuration));
    return this;
  }

  /**
   * Replaces the {@link CheckContext} of every check. The function receives the context of
   * a check, including its configuration, and returns the context the check gets instead, usually
   * a {@link ForwardingCheckContext} that overrides single methods.
   *
   * <p>The function is applied once per check and {@link Taikai} instance. All functions,
   * including those for single checks of {@link #context(Class, UnaryOperator)}, are applied in
   * the order they were configured, each receiving the result of the previous one.</p>
   *
   * @param context the function that returns the context to use
   * @return this configurer for fluent chaining
   */
  public ChecksConfigurer context(UnaryOperator<CheckContext> context) {
    this.contextFunctions.add(new ContextFunction(null, requireNonNull(context)));
    return this;
  }

  /**
   * Replaces the {@link CheckContext} of a single check, for example to give it another
   * project directory. The function receives the result of the functions configured before it,
   * and returns the context the check gets instead; other checks skip it.
   *
   * <pre>{@code
   * .context(PomCheck.class, context -> new ForwardingCheckContext(context) {
   *   @Override
   *   public Path projectDirectory() {
   *     return super.projectDirectory().resolve("parent");
   *   }
   * })
   * }</pre>
   *
   * <p>{@link Taikai.Builder#build()} fails if the check is not registered or is excluded.</p>
   *
   * @param check the class of the check
   * @param context the function that returns the context to use
   * @param <C> the type of the check
   * @return this configurer for fluent chaining
   */
  public <C extends TaikaiCheck> ChecksConfigurer context(Class<C> check,
      UnaryOperator<CheckContext> context) {
    this.contextFunctions.add(new ContextFunction(requireNonNull(check), requireNonNull(context)));
    return this;
  }

  Collection<Class<?>> excludedChecks() {
    return List.copyOf(this.excludedChecks);
  }

  void validate(Collection<? extends Class<?>> registeredChecks) {
    for (Class<?> excludedCheck : this.excludedChecks) {
      if (!registeredChecks.contains(excludedCheck)) {
        throw new TaikaiException("Check %s is excluded, but not registered in %s"
            .formatted(excludedCheck.getName(), REGISTRATION_FILE));
      }
    }

    Set<Class<?>> configuredChecks = new LinkedHashSet<>(this.configurations.keySet());
    this.contextFunctions.stream()
        .map(ContextFunction::check)
        .filter(Objects::nonNull)
        .forEach(configuredChecks::add);

    for (Class<?> configuredCheck : configuredChecks) {
      if (this.excludedChecks.contains(configuredCheck)) {
        throw new TaikaiException("Check %s is configured, but excluded"
            .formatted(configuredCheck.getName()));
      }

      if (!registeredChecks.contains(configuredCheck)) {
        throw new TaikaiException("Check %s is configured, but not registered in %s"
            .formatted(configuredCheck.getName(), REGISTRATION_FILE));
      }
    }
  }

  @Nullable Object configuration(Class<?> check) {
    return this.configurations.get(check);
  }

  CheckContext decorate(CheckContext context) {
    Class<?> check = context.check();
    CheckContext decoratedContext = context;

    for (ContextFunction contextFunction : this.contextFunctions) {
      if (contextFunction.appliesTo(check)) {
        decoratedContext = requireNonNull(contextFunction.function().apply(decoratedContext),
            () -> "The context function for check %s must not return null"
                .formatted(check.getName()));
      }
    }

    return decoratedContext;
  }

  private record ContextFunction(@Nullable Class<?> check, UnaryOperator<CheckContext> function) {

    boolean appliesTo(Class<?> check) {
      return this.check == null || this.check == check;
    }
  }
}
