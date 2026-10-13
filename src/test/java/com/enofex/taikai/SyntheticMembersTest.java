package com.enofex.taikai;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PUBLIC;
import static com.tngtech.archunit.core.domain.JavaModifier.SYNTHETIC;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compiler-generated members, such as enum {@code $values()}, generic bridge methods and
 * {@code $assertionsDisabled}, must not be reported by rules that select methods or fields.
 */
class SyntheticMembersTest {

  @Test
  void shouldIgnoreEnumValuesMethodForMethodNaming() {
    assertHasSyntheticMethod(Status.class);

    Taikai taikai = Taikai.builder()
        .classes(Status.class)
        .java(java -> java.naming(naming -> naming.methodsShouldMatch("[a-z][a-zA-Z0-9]*")))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldIgnoreEnumValuesMethodForMethodModifiers() {
    Taikai taikai = Taikai.builder()
        .classes(Status.class)
        .java(java -> java.methodsShouldHaveModifiersForClass(ClassCriteria.nameMatching(".*Status"), List.of(PUBLIC)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldIgnoreBridgeMethodsForRequiredReturnTypes() {
    assertHasSyntheticMethod(OrderSupplier.class);

    Taikai taikai = Taikai.builder()
        .classes(OrderSupplier.class, OrderController.class)
        .java(java -> java.methodsShouldReturnForClass(
            ClassCriteria.simpleNameMatching("OrderSupplier"), ClassCriteria.nameMatching(".*DTO")))
        .spring(spring -> spring.controllers(controllers -> controllers
            .methodsShouldReturn(ClassCriteria.nameMatching(".*DTO"))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldStillReportDeclaredMethodsWithoutRequiredReturnType() {
    Taikai taikai = Taikai.builder()
        .classes(OrderController.class)
        .spring(spring -> spring.controllers(controllers -> controllers
            .methodsShouldReturn(ClassCriteria.simpleNameMatching("OrderSupplier"))))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldIgnoreErasedThrowsClauseOfBridgeMethods() {
    assertHasSyntheticMethod(LoadingTask.class);

    Taikai taikai = Taikai.builder()
        .classes(LoadingTask.class)
        .java(java -> java.methodsShouldNotDeclareGenericExceptions())
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldIgnoreBridgeMethodsForAnnotatedMethodModifiers() {
    assertHasSyntheticMethod(TransactionalHandler.class);

    Taikai taikai = Taikai.builder()
        .classes(TransactionalHandler.class)
        .java(java -> java.methodsShouldHaveModifiers(MethodCriteria.annotatedWith(Proxied.class), List.of(FINAL)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldIgnoreAssertionsDisabledFieldForFieldNaming() {
    assertTrue(new ClassFileImporter().importClass(AssertingService.class).getFields().stream()
        .anyMatch(field -> field.getModifiers().contains(SYNTHETIC)));

    Taikai taikai = Taikai.builder()
        .classes(AssertingService.class)
        .java(java -> java.naming(naming -> naming.fieldsShouldMatch(FieldCriteria.ofType(boolean.class), "is.*")))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldIgnoreBridgeMethodsCallingProxiedMethods() {
    Taikai taikai = Taikai.builder()
        .classes(TransactionalHandler.class)
        .spring(spring -> spring.noSelfInvocationOfProxiedMethods(List.of(Proxied.class.getName())))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldStillReportRealSelfInvocationOfProxiedMethods() {
    Taikai taikai = Taikai.builder()
        .classes(SelfInvokingHandler.class)
        .spring(spring -> spring.noSelfInvocationOfProxiedMethods(List.of(Proxied.class.getName())))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  private static void assertHasSyntheticMethod(Class<?> type) {
    JavaClass imported = new ClassFileImporter().importClass(type);
    assertTrue(imported.getMethods().stream()
        .anyMatch(method -> method.getModifiers().contains(SYNTHETIC)));
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Proxied {
  }

  enum Status {
    ACTIVE
  }

  static class OrderDTO {
  }

  static class OrderSupplier implements Supplier<OrderDTO> {

    @Override
    public OrderDTO get() {
      return new OrderDTO();
    }
  }

  @RestController
  static class OrderController implements Supplier<OrderDTO> {

    @Override
    public OrderDTO get() {
      return new OrderDTO();
    }
  }

  static class LoadingTask implements Callable<String> {

    @Override
    public String call() throws IOException {
      return "loaded";
    }
  }

  static class TransactionalHandler implements Consumer<String> {

    @Proxied
    @Override
    public final void accept(String value) {
    }
  }

  static class SelfInvokingHandler {

    void handle() {
      save();
    }

    @Proxied
    void save() {
    }
  }

  static class AssertingService {

    private boolean isActive;

    void activate() {
      assert !this.isActive;
      this.isActive = true;
    }
  }
}
