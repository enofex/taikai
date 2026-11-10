package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.MethodCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

class TransactionalConfigurerTest {

  @Nested
  class MethodsShouldBePublic {

    static Stream<Named<Customizer<TransactionalConfigurer>>> rules() {
      return Stream.of(
          Named.of("methodsShouldBePublic()",
              transactional -> transactional.methodsShouldBePublic()),
          Named.of("methodsShouldBePublic(Configuration)",
              transactional -> transactional.methodsShouldBePublic(defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenTransactionalMethodIsPublic(Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPublicTransactionalMethod.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenTransactionalMethodIsPrivate(Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPrivateTransactionalMethod.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {ServiceWithPackagePrivateTransactionalMethod.class,
        ServiceWithProtectedTransactionalMethod.class,
        ServiceWithPrivateJakartaTransactionalMethod.class})
    void shouldThrowWhenTransactionalMethodIsNotPublic(Class<?> service) {
      Taikai taikai = Taikai.builder()
          .classes(service)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::methodsShouldBePublic))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenNonPublicMethodIsNotTransactional() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithoutTransactionalMethods.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::methodsShouldBePublic))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldBeReadOnly {

    static final DescribedPredicate<JavaMethod> QUERY_METHODS =
        MethodCriteria.nameMatching("(find|get|count).*");

    static Stream<Named<Customizer<TransactionalConfigurer>>> rules() {
      return Stream.of(
          Named.of("methodsShouldBeReadOnly(DescribedPredicate)",
              transactional -> transactional.methodsShouldBeReadOnly(QUERY_METHODS)),
          Named.of("methodsShouldBeReadOnly(DescribedPredicate, Configuration)",
              transactional -> transactional.methodsShouldBeReadOnly(QUERY_METHODS,
                  defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenMethodIsReadOnly(Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithReadOnlyMethod.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenMethodIsNotReadOnly(Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithReadWriteMethod.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {ReadOnlyService.class,
        ServiceExtendingReadOnlyService.class,
        ServiceImplementingReadOnlyInterface.class,
        ServiceImplementingReadOnlyInterfaceMethod.class,
        ServiceWithComposedReadOnlyAnnotation.class,
        ServiceWithReadOnlyMethodInReadWriteClass.class,
        ReadWriteServiceImplementingReadOnlyInterfaceMethod.class,
        ReadWriteServiceWithComposedAnnotationsSharingMetaAnnotation.class,
        ReadWriteServiceImplementingReadOnlyGenericReturnType.class,
        ReadWriteServiceImplementingReadOnlyGenericInterfaceMethod.class,
        ServiceWithAliasedReadOnlyAnnotations.class,
        ServiceWithMirroredReadOnlyAnnotation.class,
        JakartaServiceImplementingReadOnlyInterfaceMethod.class,
        ReadWriteServiceImplementingReadOnlyGenericArrayMethod.class})
    void shouldNotThrowWhenEffectiveTransactionIsReadOnly(Class<?> service) {
      assertDoesNotThrow(taikai(service)::check);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {ReadWriteService.class,
        ServiceExtendingReadWriteService.class,
        ServiceImplementingReadWriteInterface.class,
        ServiceImplementingReadWriteInterfaceMethod.class,
        ServiceWithComposedReadWriteAnnotation.class,
        ReadOnlyServiceImplementingReadWriteGenericInterfaceMethod.class,
        ServiceWithAliasedReadWriteAnnotation.class,
        ServiceWithJakartaTransactionalMethod.class,
        ServiceExtendingJakartaTransactionalService.class,
        ServiceImplementingOverloadedQueryMethods.class})
    void shouldThrowWhenEffectiveTransactionIsNotReadOnly(Class<?> service) {
      assertThrows(AssertionError.class, taikai(service)::check);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {ServiceWithNonTransactionalQueryMethod.class,
        ReadWriteServiceWithPrivateAndStaticQueryMethods.class,
        ServiceWithPublicTransactionalMethod.class,
        ServiceImplementingJakartaTransactionalInterfaceMethod.class,
        ServiceImplementingJakartaTransactionalInterface.class,
        ServiceExtendingServiceWithJakartaTransactionalMethod.class,
        ServiceHidingPrivateTransactionalQueryMethod.class})
    void shouldNotThrowWhenMatchingMethodIsNotTransactional(Class<?> service) {
      assertDoesNotThrow(taikai(service)::check);
    }

    @Test
    void shouldReportThatMethodTransactionReplacesReadOnlyClassTransaction() {
      AssertionError error = assertThrows(AssertionError.class,
          taikai(ReadOnlyServiceWithReadWriteMethod.class)::check);

      assertTrue(error.getMessage().contains("Method "
              + ReadOnlyServiceWithReadWriteMethod.class.getName() + ".findAll() runs in a "
              + "transaction that is not read-only (PROPAGATION_REQUIRED,ISOLATION_DEFAULT,"
              + "timeout_5), declared on the method or a method it overrides, which replaces the "
              + "read-only transaction declared on its class"),
          error.getMessage());
    }

    @Test
    void shouldReportTransactionOfOverriddenMethod() {
      AssertionError error = assertThrows(AssertionError.class,
          taikai(ServiceOverridingReadWriteMethod.class)::check);

      assertTrue(error.getMessage().contains("Method "
              + ServiceOverridingReadWriteMethod.class.getName() + ".findAll() runs in a "
              + "transaction that is not read-only (PROPAGATION_REQUIRED,ISOLATION_DEFAULT), "
              + "declared on the method or a method it overrides"),
          error.getMessage());
    }

    @Test
    void shouldReportClassTransaction() {
      AssertionError error = assertThrows(AssertionError.class,
          taikai(ServiceExtendingReadWriteService.class)::check);

      assertTrue(error.getMessage().contains("Method "
              + ServiceExtendingReadWriteService.class.getName() + ".countAll() runs in a "
              + "transaction that is not read-only (PROPAGATION_REQUIRED,ISOLATION_DEFAULT), "
              + "declared on " + ServiceExtendingReadWriteService.class.getName()
              + " or one of its supertypes"),
          error.getMessage());
    }

    @Test
    void shouldReportOnlyOverloadThatIsNotReadOnly() {
      AssertionError error = assertThrows(AssertionError.class,
          taikai(ServiceImplementingOverloadedQueryMethods.class)::check);

      assertTrue(error.getMessage().contains("findBy(java.lang.Long)"), error.getMessage());
      assertFalse(error.getMessage().contains("findBy(java.lang.String)"), error.getMessage());
    }

    private static Taikai taikai(Class<?> service) {
      return Taikai.builder()
          .classes(service)
          .spring(spring -> spring.transactional(transactional -> transactional
              .methodsShouldBeReadOnly(QUERY_METHODS)))
          .build();
    }
  }

  @Nested
  class ShouldNotBeSelfInvoked {

    static Stream<Named<Customizer<TransactionalConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldNotBeSelfInvoked()",
              transactional -> transactional.shouldNotBeSelfInvoked()),
          Named.of("shouldNotBeSelfInvoked(Configuration)",
              transactional -> transactional.shouldNotBeSelfInvoked(defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenTransactionalMethodIsCalledFromSameClass(
        Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithSelfInvokedTransactionalMethod.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {ServiceWithSelfInvokedJakartaTransactionalMethod.class,
        ServiceCallingTransactionalMethodFromConstructor.class})
    void shouldThrowWhenTransactionalMethodIsSelfInvoked(Class<?> service) {
      Taikai taikai = Taikai.builder()
          .classes(service)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::shouldNotBeSelfInvoked))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenTransactionalMethodIsCalledFromAnotherClass() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceCallingAnotherService.class, ServiceWithPublicTransactionalMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::shouldNotBeSelfInvoked))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenSelfInvokedMethodIsNotTransactional() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithSelfInvokedPlainMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::shouldNotBeSelfInvoked))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenTransactionalMethodIsNotSelfInvoked(
        Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPublicTransactionalMethod.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldNotBeUsedInControllers {

    static Stream<Named<Customizer<TransactionalConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldNotBeUsedInControllers()",
              transactional -> transactional.shouldNotBeUsedInControllers()),
          Named.of("shouldNotBeUsedInControllers(Configuration)",
              transactional -> transactional.shouldNotBeUsedInControllers(
                  defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenControllerIsNotTransactional(Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(NonTransactionalController.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenControllerIsAnnotatedWithTransactional(
        Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(TransactionalAnnotatedController.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenRestControllerDeclaresTransactionalMethod() {
      Taikai taikai = Taikai.builder()
          .classes(RestControllerWithTransactionalMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::shouldNotBeUsedInControllers))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenTransactionalIsUsedOutsideControllers() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPublicTransactionalMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::shouldNotBeUsedInControllers))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableTransactionalConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPrivateTransactionalMethod.class)
          .spring(spring -> spring.transactional(transactional -> {
            transactional.methodsShouldBePublic();
            transactional.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Service
  static class ServiceWithPublicTransactionalMethod {

    @Transactional
    public void save() {
    }
  }

  @Service
  static class ServiceWithPackagePrivateTransactionalMethod {

    @Transactional
    void save() {
    }
  }

  @Service
  static class ServiceWithProtectedTransactionalMethod {

    @Transactional
    protected void save() {
    }
  }

  @Service
  static class ServiceWithPrivateTransactionalMethod {

    @Transactional
    private void save() {
    }
  }

  @Service
  static class ServiceWithPrivateJakartaTransactionalMethod {

    @jakarta.transaction.Transactional
    private void save() {
    }
  }

  @Service
  static class ServiceWithoutTransactionalMethods {

    private void helper() {
    }
  }

  @Service
  static class ServiceWithReadOnlyMethod {

    @Transactional(readOnly = true)
    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  static class ServiceWithReadWriteMethod {

    @Transactional
    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  static class ServiceOverridingReadWriteMethod extends ServiceWithReadWriteMethod {

    @Override
    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  @Transactional(readOnly = true)
  static class ReadOnlyService {

    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  static class ServiceExtendingReadOnlyService extends ReadOnlyService {

    public long countAll() {
      return 0;
    }
  }

  @Transactional(readOnly = true)
  interface ReadOnlyQueries {

    List<String> findAll();
  }

  @Service
  static class ServiceImplementingReadOnlyInterface implements ReadOnlyQueries {

    @Override
    public List<String> findAll() {
      return List.of();
    }
  }

  interface ReadOnlyQueryMethods {

    @Transactional(readOnly = true)
    List<String> findAll();
  }

  @Service
  static class ServiceImplementingReadOnlyInterfaceMethod implements ReadOnlyQueryMethods {

    @Override
    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  @Transactional
  static class ReadWriteServiceImplementingReadOnlyInterfaceMethod implements
      ReadOnlyQueryMethods {

    @Override
    public List<String> findAll() {
      return List.of();
    }
  }

  interface ReadWriteQueryMethods {

    @Transactional
    List<String> findAll();
  }

  @Service
  static class ServiceImplementingReadWriteInterfaceMethod implements ReadWriteQueryMethods {

    @Override
    public List<String> findAll() {
      return List.of();
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Transactional(readOnly = true)
  @interface ReadOnlyTransactional {

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Transactional
  @interface ReadWriteTransactional {

  }

  @Service
  static class ServiceWithComposedReadOnlyAnnotation {

    @ReadOnlyTransactional
    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  static class ServiceWithComposedReadWriteAnnotation {

    @ReadWriteTransactional
    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  @Transactional
  static class ServiceWithReadOnlyMethodInReadWriteClass {

    @Transactional(readOnly = true)
    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  @Transactional
  static class ReadWriteService {

    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  static class ServiceExtendingReadWriteService extends ReadWriteService {

    public long countAll() {
      return 0;
    }
  }

  @Transactional
  interface ReadWriteQueries {

    List<String> findAll();
  }

  @Service
  static class ServiceImplementingReadWriteInterface implements ReadWriteQueries {

    @Override
    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  @Transactional(readOnly = true)
  static class ReadOnlyServiceWithReadWriteMethod {

    @Transactional(timeout = 5)
    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  static class ServiceWithJakartaTransactionalMethod {

    @jakarta.transaction.Transactional
    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  static class ServiceWithNonTransactionalQueryMethod {

    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  @Transactional
  static class ReadWriteServiceWithPrivateAndStaticQueryMethods {

    public void process() {
      findCached();
    }

    private List<String> findCached() {
      return List.of();
    }

    static long countAll() {
      return 0;
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Monitored {

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Monitored
  @interface Audited {

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Monitored
  @ReadOnlyTransactional
  @interface MonitoredQuery {

  }

  @Service
  @Transactional
  static class ReadWriteServiceWithComposedAnnotationsSharingMetaAnnotation {

    @Audited
    @MonitoredQuery
    public List<String> findAll() {
      return List.of();
    }
  }

  interface ReadOnlyGenericQueries<T> {

    @Transactional(readOnly = true)
    T findFirst();
  }

  @Service
  @Transactional
  static class ReadWriteServiceImplementingReadOnlyGenericReturnType implements
      ReadOnlyGenericQueries<String> {

    @Override
    public String findFirst() {
      return "";
    }
  }

  interface ReadOnlyRepository<T, ID> {

    @Transactional(readOnly = true)
    T findById(ID id);
  }

  interface StringReadOnlyRepository<ID> extends ReadOnlyRepository<String, ID> {

  }

  @Service
  @Transactional
  static class ReadWriteServiceImplementingReadOnlyGenericInterfaceMethod implements
      StringReadOnlyRepository<Long> {

    @Override
    public String findById(Long id) {
      return "";
    }
  }

  interface ReadWriteRepository<T, ID> {

    @Transactional
    T findById(ID id);
  }

  @Service
  @Transactional(readOnly = true)
  static class ReadOnlyServiceImplementingReadWriteGenericInterfaceMethod implements
      ReadWriteRepository<String, Long> {

    @Override
    public String findById(Long id) {
      return "";
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Transactional
  @interface ServiceTransactional {

    @AliasFor(annotation = Transactional.class, attribute = "readOnly")
    boolean readOnly() default false;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Transactional
  @interface QueryTransactional {

    @AliasFor(annotation = Transactional.class)
    boolean readOnly() default true;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @ServiceTransactional
  @interface ReportQuery {

    @AliasFor(annotation = ServiceTransactional.class, attribute = "readOnly")
    boolean readOnly() default true;
  }

  @Service
  static class ServiceWithAliasedReadOnlyAnnotations {

    @ServiceTransactional(readOnly = true)
    public List<String> findAll() {
      return List.of();
    }

    @QueryTransactional
    public long countAll() {
      return 0;
    }

    @ReportQuery
    public List<String> getReport() {
      return List.of();
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Transactional
  @interface ReadOnlyQuery {

    @AliasFor(annotation = Transactional.class, attribute = "readOnly")
    boolean value() default false;

    @AliasFor(annotation = Transactional.class, attribute = "readOnly")
    boolean readOnly() default false;
  }

  @Service
  static class ServiceWithMirroredReadOnlyAnnotation {

    @ReadOnlyQuery(true)
    public List<String> findAll() {
      return List.of();
    }
  }

  interface ReadOnlyBatchRepository<T, ID> {

    @Transactional(readOnly = true)
    List<T> findAllById(ID[] ids);
  }

  @Service
  @Transactional
  static class ReadWriteServiceImplementingReadOnlyGenericArrayMethod implements
      ReadOnlyBatchRepository<String, Long> {

    @Override
    public List<String> findAllById(Long[] ids) {
      return List.of();
    }
  }

  @Service
  static class ServiceWithAliasedReadWriteAnnotation {

    @QueryTransactional(readOnly = false)
    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  static class JakartaServiceImplementingReadOnlyInterfaceMethod implements
      ReadOnlyQueryMethods {

    @Override
    @jakarta.transaction.Transactional
    public List<String> findAll() {
      return List.of();
    }
  }

  interface JakartaTransactionalQueryMethods {

    @jakarta.transaction.Transactional
    List<String> findAll();
  }

  @Service
  static class ServiceImplementingJakartaTransactionalInterfaceMethod implements
      JakartaTransactionalQueryMethods {

    @Override
    public List<String> findAll() {
      return List.of();
    }
  }

  @jakarta.transaction.Transactional
  interface JakartaTransactionalQueries {

    List<String> findAll();
  }

  @Service
  static class ServiceImplementingJakartaTransactionalInterface implements
      JakartaTransactionalQueries {

    @Override
    public List<String> findAll() {
      return List.of();
    }
  }

  @Service
  static class ServiceExtendingServiceWithJakartaTransactionalMethod extends
      ServiceWithJakartaTransactionalMethod {

    @Override
    public List<String> findAll() {
      return List.of();
    }
  }

  @jakarta.transaction.Transactional
  static class JakartaTransactionalService {

  }

  @Service
  static class ServiceExtendingJakartaTransactionalService extends JakartaTransactionalService {

    public List<String> findAll() {
      return List.of();
    }
  }

  static class ServiceWithPrivateTransactionalQueryMethod {

    @Transactional
    private List<String> findAll() {
      return List.of();
    }
  }

  @Service
  static class ServiceHidingPrivateTransactionalQueryMethod extends
      ServiceWithPrivateTransactionalQueryMethod {

    public List<String> findAll() {
      return List.of();
    }
  }

  interface OverloadedQueryMethods {

    @Transactional(readOnly = true)
    List<String> findBy(String name);

    @Transactional
    List<String> findBy(Long id);
  }

  @Service
  static class ServiceImplementingOverloadedQueryMethods implements OverloadedQueryMethods {

    @Override
    public List<String> findBy(String name) {
      return List.of();
    }

    @Override
    public List<String> findBy(Long id) {
      return List.of();
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
  static class ServiceWithSelfInvokedJakartaTransactionalMethod {

    public void process() {
      save();
    }

    @jakarta.transaction.Transactional
    public void save() {
    }
  }

  @Service
  static class ServiceCallingTransactionalMethodFromConstructor {

    ServiceCallingTransactionalMethodFromConstructor() {
      save();
    }

    @Transactional
    public void save() {
    }
  }

  @Service
  static class ServiceWithSelfInvokedPlainMethod {

    public void process() {
      save();
    }

    public void save() {
    }
  }

  @Service
  static class ServiceCallingAnotherService {

    private final ServiceWithPublicTransactionalMethod delegate;

    ServiceCallingAnotherService(ServiceWithPublicTransactionalMethod delegate) {
      this.delegate = delegate;
    }

    public void process() {
      this.delegate.save();
    }
  }

  @Controller
  static class NonTransactionalController {

    public void handle() {
    }
  }

  @Controller
  @Transactional
  static class TransactionalAnnotatedController {

  }

  @RestController
  static class RestControllerWithTransactionalMethod {

    @Transactional
    public void handle() {
    }
  }
}
