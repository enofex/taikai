package com.enofex.taikai.checks.spi;

import static java.util.Objects.requireNonNull;

import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * A {@link CheckContext} that forwards every method to another context. Subclasses override only
 * the methods they change, and keep working when methods are added to {@link CheckContext}.
 *
 * <pre>{@code
 * .discoverChecks(checks -> checks
 *     .context(context -> new ForwardingCheckContext(context) {
 *       @Override
 *       public ClassLoader classLoader() {
 *         return myClassLoader;
 *       }
 *     }))
 * }</pre>
 */
public abstract class ForwardingCheckContext implements CheckContext {

  private final CheckContext context;

  /**
   * Creates a context that forwards to the given context.
   *
   * @param context the context to forward to
   */
  protected ForwardingCheckContext(CheckContext context) {
    this.context = requireNonNull(context);
  }

  @Override
  public Class<? extends TaikaiCheck> check() {
    return this.context.check();
  }

  @Override
  public Path projectDirectory() {
    return this.context.projectDirectory();
  }

  @Override
  public @Nullable String namespace() {
    return this.context.namespace();
  }

  @Override
  public JavaClasses classes() {
    return this.context.classes();
  }

  @Override
  public ClassLoader classLoader() {
    return this.context.classLoader();
  }

  @Override
  public <T> Optional<T> configuration(Class<T> type) {
    return this.context.configuration(type);
  }
}
