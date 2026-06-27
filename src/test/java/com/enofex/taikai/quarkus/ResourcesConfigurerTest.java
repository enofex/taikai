package com.enofex.taikai.quarkus;

import com.enofex.taikai.Taikai;

import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

  @Nested
  class FluentChaining {

    @Test
    void shouldSupportChainingAllMethods() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(r ->
              r.namesShouldEndWithResource()
                  .shouldBePublic()
                  .shouldBeAnnotatedWithPath()
                  .shouldNotDependOnOtherResources()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportChainingWithConfigurationOverloads() {
      var config = com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration();
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(r ->
              r.namesShouldEndWithResource(config)
                  .shouldBePublic(config)
                  .shouldBeAnnotatedWithPath(config)
                  .shouldNotDependOnOtherResources(config)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldReturnNonNullFromNamesShouldMatchWithRegex() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(r -> {
            ResourcesConfigurer chained = r.namesShouldMatch(".+Resource");
            assertNotNull(chained, "namesShouldMatch(String) must not return null");
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldReturnNonNullFromShouldBeAnnotatedWithPathWithRegex() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(r -> {
            ResourcesConfigurer chained = r.shouldBeAnnotatedWithPath(".+Resource");
            assertNotNull(chained, "shouldBeAnnotatedWithPath(String) must not return null");
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldReturnNonNullFromShouldNotDependOnOtherResourcesNoArg() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(r -> {
            ResourcesConfigurer chained = r.shouldNotDependOnOtherResources();
            assertNotNull(chained, "shouldNotDependOnOtherResources() must not return null");
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldReturnNonNullFromShouldNotDependOnOtherResourcesWithConfig() {
      var config = com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration();
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(r -> {
            ResourcesConfigurer chained = r.shouldNotDependOnOtherResources(config);
            assertNotNull(chained, "shouldNotDependOnOtherResources(Configuration) must not return null");
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldReturnNonNullFromDisable() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(r -> {
            ResourcesConfigurer chained = r.disable();
            assertNotNull(chained, "disable() must not return null");
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Path("/test")
  static public class BadController {
  }

  @Nested
  class NegativeDelegation {

    @Test
    void shouldThrowWhenResourceNameDoesNotEndWithResource() {
      Taikai taikai = Taikai.builder()
          .classes(BadController.class)
          .quarkus(quarkus -> quarkus.resources(ResourcesConfigurer::namesShouldEndWithResource))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenResourceNameDoesNotEndWithResourceWithConfig() {
      Taikai taikai = Taikai.builder()
          .classes(BadController.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.namesShouldEndWithResource(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenResourceWithoutPathAnnotationWithConfig() {
      Taikai taikai = Taikai.builder()
          .classes(MissingRestResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.shouldBeAnnotatedWithPath(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingResourceMissingPathAnnotationWithConfig() {
      Taikai taikai = Taikai.builder()
          .classes(MissingRestResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.shouldBeAnnotatedWithPath(".+Resource",
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenResourceIsPrivateWithConfig() {
      Taikai taikai = Taikai.builder()
          .classes(PrivateResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.shouldBePublic(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenResourceDependsOnOtherResourceWithConfig() {
      Taikai taikai = Taikai.builder()
          .classes(DependentResource.class, UserResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.shouldNotDependOnOtherResources(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenResourceNameDoesNotMatchRegexWithConfig() {
      Taikai taikai = Taikai.builder()
          .classes(UserResource.class)
          .quarkus(quarkus -> quarkus.resources(
              res -> res.namesShouldMatch(".+Handler",
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

}
