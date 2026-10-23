package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule.Configuration;
import jakarta.inject.Inject;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
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
  class NoInjectionIntoStaticFields {

    @Test
    void shouldThrowWhenStaticFieldAnnotatedWithValue() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticValueField.class)
          .spring(SpringConfigurer::noInjectionIntoStaticFields)
          .build();

      AssertionError error = assertThrows(AssertionError.class, taikai::check);
      assertTrue(error.getMessage().contains(
          ClassWithStaticValueField.class.getName() + ".timeout"), error.getMessage());
    }

    @Test
    void shouldThrowWhenStaticFieldAnnotatedWithAutowired() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticAutowiredField.class)
          .spring(SpringConfigurer::noInjectionIntoStaticFields)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenStaticFieldAnnotatedWithInject() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticInjectField.class)
          .spring(SpringConfigurer::noInjectionIntoStaticFields)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenStaticSetterAnnotatedWithValue() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticValueSetter.class)
          .spring(SpringConfigurer::noInjectionIntoStaticFields)
          .build();

      AssertionError error = assertThrows(AssertionError.class, taikai::check);
      assertTrue(error.getMessage().contains(
          ClassWithStaticValueSetter.class.getName() + ".setTimeout(int)"), error.getMessage());
    }

    @Test
    void shouldThrowWhenStaticFieldAnnotatedWithComposedValueAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticComposedValueField.class)
          .spring(SpringConfigurer::noInjectionIntoStaticFields)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenInstanceMembersAreInjected() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithInstanceInjection.class)
          .spring(SpringConfigurer::noInjectionIntoStaticFields)
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenParameterOfStaticBeanMethodAnnotatedWithValue() {
      Taikai taikai = Taikai.builder()
          .classes(ConfigurationWithStaticBeanMethod.class)
          .spring(SpringConfigurer::noInjectionIntoStaticFields)
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationOverload() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithStaticValueField.class)
          .spring(spring -> spring
              .noInjectionIntoStaticFields(Configuration.defaultConfiguration()))
          .build();

      assertThrows(AssertionError.class, taikai::check);
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

  static class ClassWithStaticValueField {

    @Value("${timeout}")
    private static int timeout;
  }

  static class ClassWithStaticAutowiredField {

    @Autowired
    private static String dependency;
  }

  static class ClassWithStaticInjectField {

    @Inject
    private static String dependency;
  }

  static class ClassWithStaticValueSetter {

    private static int timeout;

    @Value("${timeout}")
    static void setTimeout(int timeout) {
      ClassWithStaticValueSetter.timeout = timeout;
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  @Value("${server.port}")
  @interface ServerPort {
  }

  static class ClassWithStaticComposedValueField {

    @ServerPort
    private static int port;
  }

  static class ClassWithInstanceInjection {

    private static final int DEFAULT_TIMEOUT = 30;

    @Value("${timeout}")
    private int timeout = DEFAULT_TIMEOUT;

    private String dependency;

    @Autowired
    void setDependency(String dependency) {
      this.dependency = dependency;
    }
  }

  @org.springframework.context.annotation.Configuration
  static class ConfigurationWithStaticBeanMethod {

    @Bean
    static String name(@Value("${name}") String name) {
      return name;
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
