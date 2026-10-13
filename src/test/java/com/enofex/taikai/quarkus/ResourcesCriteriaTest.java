package com.enofex.taikai.quarkus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

class ResourcesCriteriaTest {

  @Test
  void shouldNotThrowWhenSelectedResourcesAreAnnotatedWithPath() {
    Taikai taikai = Taikai.builder()
        .classes(OrderEndpoint.class, EndpointContract.class)
        .quarkus(quarkus -> quarkus.resources(resources -> resources
            .shouldBeAnnotatedWithPath(ClassCriteria.nameMatching(".*Endpoint.*"))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowWhenSelectedResourcesMissPathAnnotation() {
    Taikai taikai = Taikai.builder()
        .classes(OrderEndpoint.class, PlainEndpoint.class)
        .quarkus(quarkus -> quarkus.resources(resources -> resources
            .shouldBeAnnotatedWithPath(ClassCriteria.nameMatching(".*Endpoint"))))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldIgnoreClassesNotMatchingCriteria() {
    Taikai taikai = Taikai.builder()
        .classes(PlainEndpoint.class)
        .quarkus(quarkus -> quarkus.resources(resources -> resources
            .shouldBeAnnotatedWithPath(ClassCriteria.inPackage("com.company.rest.."))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Path("/orders")
  static class OrderEndpoint {
  }

  static class PlainEndpoint {
  }

  interface EndpointContract {
  }
}
