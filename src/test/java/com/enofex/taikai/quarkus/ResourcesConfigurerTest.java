package com.enofex.taikai.quarkus;

import com.enofex.taikai.Taikai;

import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ResourcesConfigurerTest {

  @Nested
  class NamesShouldEndWithResources {

    @Test
    void shouldNotThrowWhenResourceEndsWithResource() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(ResourcesConfigurer::namesShouldEndWithResource))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassIsNotAResource() {
      Taikai taikai = Taikai.builder()
          .classes(ResourcesConfigurerTest.InvalidUserHandler.class)
          .quarkus(quarkus -> quarkus.resources(ResourcesConfigurer::namesShouldEndWithResource))
          .build();

      assertDoesNotThrow(taikai::check);
    }

  }

  @Nested
  class ShouldBeAnnotatedWithPath {

    @Test
    void shouldNotThrowWhenPathAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(ResourcesConfigurerTest.UserResource.class)
          .quarkus(quarkus -> quarkus.resources(
              ResourcesConfigurer::shouldBeAnnotatedWithPath))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenResourceMissingPathAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ResourcesConfigurerTest.MissingRestResource.class)
          .quarkus(quarkus -> quarkus.resources(
              ResourcesConfigurer::shouldBeAnnotatedWithPath))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenMatchingResourceHasPathAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ResourcesConfigurerTest.UserResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.shouldBeAnnotatedWithPath(".+Resource")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingResourceMissingPathAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ResourcesConfigurerTest.MissingRestResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.shouldBeAnnotatedWithPath(".+Resource")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBePublic {

    @Test
    void shouldThrowWhenResourceIsPrivate() {
      Taikai taikai = Taikai.builder()
          .classes(ResourcesConfigurerTest.PrivateResource.class)
          .quarkus(quarkus -> quarkus.resources(ResourcesConfigurer::shouldBePublic))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenResourceIsPublic() {
      Taikai taikai = Taikai.builder()
          .classes(ResourcesConfigurerTest.UserResource.class)
          .quarkus(quarkus -> quarkus.resources(ResourcesConfigurer::shouldBePublic))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }


  @Nested
  class NamesShouldMatch {

    @Test
    void shouldNotThrowWhenResourceNameMatchesRegex() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.namesShouldMatch(".+Resource")))
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
  class ShouldNotDependOnOtherResources {

    @Test
    void shouldNotThrowWhenResourceDoesNotDependOnOtherResource() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(
              ResourcesConfigurer::shouldNotDependOnOtherResources))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenResourceDependsOnOtherResource() {
      Taikai taikai = Taikai.builder()
          .classes(DependentResource.class, UserResource.class)
          .quarkus(quarkus -> quarkus.resources(
              ResourcesConfigurer::shouldNotDependOnOtherResources))
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

  @Nested
  class ConfigurationOverloads {

    @Test
    void shouldSupportConfigurationForNamesShouldEndWithResource() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.namesShouldEndWithResource(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldBePublic() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.shouldBePublic(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldBeAnnotatedWithPath() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.shouldBeAnnotatedWithPath(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldNotDependOnOtherResources() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.shouldNotDependOnOtherResources(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
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

}
