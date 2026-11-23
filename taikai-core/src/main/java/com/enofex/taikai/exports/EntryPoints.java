package com.enofex.taikai.exports;

import static java.util.Objects.requireNonNull;

import com.tngtech.archunit.core.domain.Formatters;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

/**
 * Finds the entry points of the measured classes: the methods that a framework calls rather than
 * other measured classes, such as the handler methods of Spring MVC controllers.
 * {@link MetricsMarkdownExport} lists them next to the fan-in of their classes, so that a class
 * that only a framework calls is not taken for unused, as configured with
 * {@link MetricsMarkdownExport.Settings#withEntryPoints(EntryPoints...)}. They do not count as
 * couplings.
 *
 * <p>Other frameworks plug in with a lambda, for example for the resource methods of JAX-RS:</p>
 *
 * <pre>{@code
 * EntryPoints jaxRs = (javaClass, method) -> method.getAnnotations().stream()
 *     .filter(annotation -> annotation.getRawType().isAnnotatedWith("jakarta.ws.rs.HttpMethod"))
 *     .map(annotation -> annotation.getRawType().getSimpleName())
 *     .collect(Collectors.toSet());
 * }</pre>
 */
@FunctionalInterface
public interface EntryPoints {

  /**
   * Returns the kinds of entry point that the framework calls through a method of a class, such
   * as {@code GET} and {@code POST}, or none if the method is no entry point.
   *
   * <p>The export asks for the methods of every concrete measured class, together with the
   * methods it inherits, and for every method also for the methods it overrides, from the class up
   * through its superclasses and then its interfaces. The first kinds found count once for the
   * class, so a method is an entry point as well if it only overrides an entry point, as an
   * implementation of an interface generated from an OpenAPI specification does.</p>
   *
   * @param javaClass the concrete class whose objects the framework calls
   * @param method a method of the class or one that it inherits or overrides, so it may be
   *     declared in a superclass or an interface
   * @return the kinds, or an empty set, but neither {@code null} nor with {@code null} in it
   */
  Set<String> kinds(JavaClass javaClass, JavaMethod method);

  /**
   * Returns the handler methods of Spring MVC controllers, the classes annotated with
   * {@code @Controller} or {@code @RestController} themselves, as the component scan of Spring
   * finds them: the methods annotated with {@code @RequestMapping} or its composed variants, such
   * as {@code @GetMapping}, also composed annotations of the project. Their kinds are their HTTP
   * methods, together with those of the {@code @RequestMapping} of the controller, also through a
   * superclass or an interface, as Spring combines them, or {@code ANY} if neither restricts
   * them.
   *
   * @return the entry points
   */
  static EntryPoints springMvc() {
    return SpringMvcEntryPoints::kinds;
  }

  /**
   * Returns the methods annotated with the given annotation, or with an annotation that is itself
   * annotated with it, such as {@code @Scheduled}, {@code @EventListener} or
   * {@code @KafkaListener}. Their kind is the simple name of the annotation, such as
   * {@code Scheduled}.
   *
   * @param annotation the fully qualified name of the annotation
   * @return the entry points
   */
  static EntryPoints annotatedWith(String annotation) {
    Set<String> kinds = Set.of(Formatters.ensureSimpleName(requireNonNull(annotation)));

    return (javaClass, method) -> method.isMetaAnnotatedWith(annotation) ? kinds : Set.of();
  }

  /**
   * See {@link #annotatedWith(String)}.
   *
   * @param annotation the annotation
   * @return the entry points
   * @throws IllegalArgumentException if the annotation has the retention {@code SOURCE}, so that
   *     no class file has it
   */
  static EntryPoints annotatedWith(Class<? extends Annotation> annotation) {
    Retention retention = annotation.getAnnotation(Retention.class);

    if (retention != null && retention.value() == RetentionPolicy.SOURCE) {
      throw new IllegalArgumentException(
          "The annotation %s has the retention SOURCE, so no class file has it".formatted(
              annotation.getName()));
    }

    return annotatedWith(annotation.getName());
  }
}
