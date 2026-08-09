package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule.Configuration;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

class SpringConfigurerTest {

  @Nested
  class NoAutowiredFields {

    @Test
    void shouldThrowWhenFieldAnnotatedWithAutowired() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAutowiredField.class)
          .spring(SpringConfigurer::noAutowiredFields)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenNoFieldAnnotatedWithAutowired() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithConstructorInjection.class)
          .spring(SpringConfigurer::noAutowiredFields)
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class NoSelfInvocationOfProxiedMethods {

    @Test
    void shouldThrowWhenAsyncMethodIsCalledFromSameClass() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithSelfInvokedAsyncMethod.class)
          .spring(SpringConfigurer::noSelfInvocationOfProxiedMethods)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenCacheableMethodIsCalledFromSameClass() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithSelfInvokedCacheableMethod.class)
          .spring(SpringConfigurer::noSelfInvocationOfProxiedMethods)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenCacheEvictMethodIsCalledFromSameClass() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithSelfInvokedCacheEvictMethod.class)
          .spring(SpringConfigurer::noSelfInvocationOfProxiedMethods)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenTransactionalMethodIsCalledFromSameClass() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithSelfInvokedTransactionalMethod.class)
          .spring(SpringConfigurer::noSelfInvocationOfProxiedMethods)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenProxiedMethodIsCalledFromAnotherClass() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceCallingAnotherService.class, ServiceWithAsyncMethod.class)
          .spring(SpringConfigurer::noSelfInvocationOfProxiedMethods)
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenSelfInvokedMethodIsNotProxied() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithSelfInvokedPlainMethod.class)
          .spring(SpringConfigurer::noSelfInvocationOfProxiedMethods)
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotationIsNotPartOfCustomAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithSelfInvokedAsyncMethod.class)
          .spring(spring -> spring.noSelfInvocationOfProxiedMethods(
              List.of("org.springframework.transaction.annotation.Transactional")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotationIsPartOfCustomAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithSelfInvokedAsyncMethod.class)
          .spring(spring -> spring.noSelfInvocationOfProxiedMethods(
              List.of("org.springframework.scheduling.annotation.Async")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportConfigurationOverloads() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithSelfInvokedPlainMethod.class)
          .spring(spring -> spring
              .noSelfInvocationOfProxiedMethods(Configuration.defaultConfiguration())
              .noSelfInvocationOfProxiedMethods(
                  List.of("org.springframework.scheduling.annotation.Async"),
                  Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableSpringConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithAutowiredField.class)
          .spring(spring -> {
            spring.noAutowiredFields();
            spring.disable();
          })
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  static class ClassWithAutowiredField {

    @Autowired
    private String dependency;
  }

  static class ClassWithConstructorInjection {

    private final String dependency;

    ClassWithConstructorInjection(String dependency) {
      this.dependency = dependency;
    }
  }

  @Service
  static class ServiceWithSelfInvokedAsyncMethod {

    public void process() {
      send();
    }

    @Async
    public void send() {
    }
  }

  @Service
  static class ServiceWithSelfInvokedCacheableMethod {

    public void process() {
      load();
    }

    @Cacheable("values")
    public String load() {
      return "value";
    }
  }

  @Service
  static class ServiceWithSelfInvokedCacheEvictMethod {

    public void process() {
      evict();
    }

    @CacheEvict("values")
    public void evict() {
    }
  }

  @Service
  static class ServiceWithSelfInvokedTransactionalMethod {

    public void process() {
      save();
    }

    @Transactional
    public void save() {
    }
  }

  @Service
  static class ServiceWithSelfInvokedPlainMethod {

    public void process() {
      send();
    }

    public void send() {
    }
  }

  @Service
  static class ServiceWithAsyncMethod {

    @Async
    public void send() {
    }
  }

  @Service
  static class ServiceCallingAnotherService {

    private final ServiceWithAsyncMethod delegate;

    ServiceCallingAnotherService(ServiceWithAsyncMethod delegate) {
      this.delegate = delegate;
    }

    public void process() {
      this.delegate.send();
    }
  }

  @Test
  void shouldReturnAnnotatedWithValidatedPredicate() {
    assertNotNull(SpringDescribedPredicates.annotatedWithValidated(true));
    assertNotNull(SpringDescribedPredicates.annotatedWithValidated(false));
  }
}
