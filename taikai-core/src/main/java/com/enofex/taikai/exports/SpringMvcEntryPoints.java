package com.enofex.taikai.exports;

import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_CONTROLLER;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_REQUEST_MAPPING;
import static java.util.stream.Collectors.toSet;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.properties.HasAnnotations;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The handler methods of Spring MVC controllers, found by the names of the annotations, so that
 * Spring need not be on the classpath. See {@link EntryPoints#springMvc()}.
 */
final class SpringMvcEntryPoints {

  /**
   * The kind of a mapping that does not restrict the HTTP methods.
   */
  private static final String ANY = "ANY";

  private SpringMvcEntryPoints() {
  }

  static Set<String> kinds(JavaClass javaClass, JavaMethod method) {
    if (!isController(javaClass)) {
      return Set.of();
    }

    return requestMapping(method)
        .map(requestMapping -> httpMethods(requestMapping, javaClass))
        .orElse(Set.of());
  }

  /**
   * Returns whether the class itself is annotated with {@code @Controller}, directly or through
   * an annotation such as {@code @RestController}. The component scan of Spring finds only such
   * classes, as the annotation is not inherited, so the subclass of a controller is none unless it
   * is annotated itself.
   */
  private static boolean isController(JavaClass javaClass) {
    return javaClass.isMetaAnnotatedWith(ANNOTATION_CONTROLLER);
  }

  /**
   * Returns the HTTP methods of the mapping of the method together with those of the mapping of
   * the controller, which Spring combines, or {@code ANY} if neither restricts them.
   */
  private static Set<String> httpMethods(JavaAnnotation<?> requestMapping, JavaClass javaClass) {
    Set<String> httpMethods = Stream.concat(Stream.of(requestMapping),
            controllerMapping(javaClass).stream())
        .flatMap(SpringMvcEntryPoints::httpMethods)
        .collect(toSet());

    return httpMethods.isEmpty() ? Set.of(ANY) : httpMethods;
  }

  /**
   * Returns the {@code @RequestMapping} of the class, or else the first one of its interfaces and
   * then of its superclass, each with their own interfaces and superclasses, in the order in which
   * Spring searches them.
   */
  private static Optional<JavaAnnotation<?>> controllerMapping(JavaClass type) {
    return requestMapping(type)
        .or(() -> type.getRawInterfaces().stream()
            .flatMap(anInterface -> controllerMapping(anInterface).stream())
            .findFirst())
        .or(() -> type.getRawSuperclass().flatMap(SpringMvcEntryPoints::controllerMapping));
  }

  /**
   * Returns the HTTP methods of a mapping. ArchUnit returns an empty array, such as the default,
   * as an {@code Object[]}, as it has no type of its elements.
   */
  private static Stream<String> httpMethods(JavaAnnotation<?> requestMapping) {
    return requestMapping.get("method").stream()
        .flatMap(httpMethods -> Arrays.stream((Object[]) httpMethods))
        .map(httpMethod -> ((JavaEnumConstant) httpMethod).name());
  }

  private static Optional<JavaAnnotation<?>> requestMapping(HasAnnotations<?> element) {
    return requestMapping(element, new HashSet<>());
  }

  /**
   * Returns the {@code @RequestMapping} of the element, or else the one that annotates one of its
   * annotations, such as the one with {@code method = GET} on {@code @GetMapping}.
   *
   * @param visited the annotations searched already, as annotations may annotate each other
   */
  private static Optional<JavaAnnotation<?>> requestMapping(HasAnnotations<?> element,
      Set<String> visited) {
    Optional<? extends JavaAnnotation<?>> requestMapping =
        element.tryGetAnnotationOfType(ANNOTATION_REQUEST_MAPPING);

    if (requestMapping.isPresent()) {
      return Optional.of(requestMapping.get());
    }

    for (JavaAnnotation<?> annotation : element.getAnnotations()) {
      JavaClass type = annotation.getRawType();

      if (visited.add(type.getName())) {
        Optional<JavaAnnotation<?>> metaMapping = requestMapping(type, visited);

        if (metaMapping.isPresent()) {
          return metaMapping;
        }
      }
    }

    return Optional.empty();
  }
}
