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


  @Path("/test")
  static public class UserResource {
  }

  @Path("/test")
  static public class PrivateResource {
  }

  static public class InvalidUserHandler {
  }

  static public class MissingRestResource {
  }

}
