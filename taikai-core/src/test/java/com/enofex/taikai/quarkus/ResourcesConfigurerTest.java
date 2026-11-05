package com.enofex.taikai.quarkus;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Configurers;
import com.enofex.taikai.configures.Customizer;
import jakarta.ws.rs.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ResourcesConfigurerTest {

  @Nested
  class NamesShouldEndWithResources {

    static Stream<Named<Customizer<ResourcesConfigurer>>> rules() {
      return Stream.of(
          Named.of("namesShouldEndWithResource()",
              res -> res.namesShouldEndWithResource()),
          Named.of("namesShouldEndWithResource(Configuration)",
              res -> res.namesShouldEndWithResource(defaultConfiguration())),
          Named.of("namesShouldMatch(String)",
              res -> res.namesShouldMatch(".+Resource")),
          Named.of("namesShouldMatch(String, Configuration)",
              res -> res.namesShouldMatch(".+Resource", defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenResourceEndsWithResource(Customizer<ResourcesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenResourceNameDoesNotEndWithResource(Customizer<ResourcesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(BadController.class)
          .quarkus(quarkus -> quarkus.resources(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassIsNotAResource() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidUserHandler.class)
          .quarkus(quarkus -> quarkus.resources(ResourcesConfigurer::namesShouldEndWithResource))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenResourceNameDoesNotMatchRegex() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.namesShouldMatch(".+Handler")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithPath {

    static Stream<Named<Customizer<ResourcesConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBeAnnotatedWithPath()",
              res -> res.shouldBeAnnotatedWithPath()),
          Named.of("shouldBeAnnotatedWithPath(Configuration)",
              res -> res.shouldBeAnnotatedWithPath(defaultConfiguration())),
          Named.of("shouldBeAnnotatedWithPath(DescribedPredicate)",
              res -> res.shouldBeAnnotatedWithPath(ClassCriteria.nameMatching(".+Resource"))),
          Named.of("shouldBeAnnotatedWithPath(DescribedPredicate, Configuration)",
              res -> res.shouldBeAnnotatedWithPath(ClassCriteria.nameMatching(".+Resource"),
                  defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenPathAnnotated(Customizer<ResourcesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenResourceMissingPathAnnotation(Customizer<ResourcesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(MissingRestResource.class)
          .quarkus(quarkus -> quarkus.resources(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenResourceInterfaceIsNotAnnotated(Customizer<ResourcesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(OrderResource.class, DefaultOrderResource.class)
          .quarkus(quarkus -> quarkus.resources(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldBePublic {

    static Stream<Named<Customizer<ResourcesConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBePublic()",
              res -> res.shouldBePublic()),
          Named.of("shouldBePublic(Configuration)",
              res -> res.shouldBePublic(defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenResourceIsPrivate(Customizer<ResourcesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(PrivateResource.class)
          .quarkus(quarkus -> quarkus.resources(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenResourceIsPublic(Customizer<ResourcesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldNotDependOnOtherResources {

    static Stream<Named<Customizer<ResourcesConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldNotDependOnOtherResources()",
              res -> res.shouldNotDependOnOtherResources()),
          Named.of("shouldNotDependOnOtherResources(Configuration)",
              res -> res.shouldNotDependOnOtherResources(defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenResourceDoesNotDependOnOtherResource(
        Customizer<ResourcesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenResourceDependsOnOtherResource(Customizer<ResourcesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(DependentResource.class, UserResource.class)
          .quarkus(quarkus -> quarkus.resources(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Path("/test")
  static public class UserResource {
  }

  @Path("/dependent")
  static public class DependentResource {

    private final UserResource userResource;

    public DependentResource(UserResource userResource) {
      this.userResource = userResource;
    }
  }

  @Path("/test")
  static class PrivateResource {
  }

  static public class InvalidUserHandler {
  }

  static public class MissingRestResource {
  }

  public interface OrderResource {
  }

  @Path("/orders")
  static public class DefaultOrderResource implements OrderResource {
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableResourcesConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(MissingRestResource.class)
          .quarkus(quarkus -> quarkus.resources(res -> {
            res.shouldBeAnnotatedWithPath();
            res.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class FluentChaining {

    @Test
    void shouldReturnSameConfigurerFromEveryMethod() {
      ResourcesConfigurer configurer = new ResourcesConfigurer(
          new ConfigurerContext(null, new Configurers()));

      assertAll(
          () -> assertSame(configurer, configurer.namesShouldEndWithResource()),
          () -> assertSame(configurer,
              configurer.namesShouldEndWithResource(defaultConfiguration())),
          () -> assertSame(configurer, configurer.namesShouldMatch(".+Resource")),
          () -> assertSame(configurer,
              configurer.namesShouldMatch(".+Resource", defaultConfiguration())),
          () -> assertSame(configurer, configurer.shouldBePublic()),
          () -> assertSame(configurer, configurer.shouldBePublic(defaultConfiguration())),
          () -> assertSame(configurer, configurer.shouldBeAnnotatedWithPath()),
          () -> assertSame(configurer,
              configurer.shouldBeAnnotatedWithPath(defaultConfiguration())),
          () -> assertSame(configurer,
              configurer.shouldBeAnnotatedWithPath(ClassCriteria.nameMatching(".+Resource"))),
          () -> assertSame(configurer, configurer.shouldBeAnnotatedWithPath(
              ClassCriteria.nameMatching(".+Resource"), defaultConfiguration())),
          () -> assertSame(configurer, configurer.shouldNotDependOnOtherResources()),
          () -> assertSame(configurer,
              configurer.shouldNotDependOnOtherResources(defaultConfiguration())),
          () -> assertSame(configurer, configurer.disable()));
    }
  }

  @Path("/test")
  static public class BadController {
  }
}
