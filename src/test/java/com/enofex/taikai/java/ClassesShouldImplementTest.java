package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ClassesShouldImplementTest {

  @Nested
  class ClassBasedAPI {

    @Test
    void shouldNotThrowWhenClassImplementsInterface() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceImpl.class, MyService.class)
          .java(java -> java.classesShouldImplement("ServiceImpl", MyService.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassDoesNotImplementInterface() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithoutInterface.class, MyService.class)
          .java(java -> java.classesShouldImplement("ServiceWithoutInterface", MyService.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyClass() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceImpl.class, MyService.class)
          .java(java -> java.classesShouldImplement("NonExistent", MyService.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class StringBasedAPI {

    @Test
    void shouldNotThrowWhenClassImplementsInterface() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceImpl.class, MyService.class)
          .java(java -> java.classesShouldImplement("ServiceImpl", MyService.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassDoesNotImplementInterface() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithoutInterface.class, MyService.class)
          .java(java -> java.classesShouldImplement("ServiceWithoutInterface", MyService.class.getName()))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyClass() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceImpl.class, MyService.class)
          .java(java -> java.classesShouldImplement("NonExistent", MyService.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  interface MyService {
    void execute();
  }

  static class ServiceImpl implements MyService {
    @Override
    public void execute() {
      // Implementation here
    }
  }

  static class ServiceWithoutInterface {
    public void execute() {
      // No interface implemented
    }
  }
}
