package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NoUsageOfRuleTest {

  @Nested
  class ExceptionLayerRestrictions {

    @Test
    void shouldRejectUncheckedExceptionsConstructedWithoutThrowsClause() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowsHttpException.class)
          .java(java -> java.noUsageOf(HttpStatusException.class, "com.enofex.taikai.java"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldRejectExceptionSubclassesByTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowsHttpExceptionSubclass.class)
          .java(java -> java.noUsageOf(HttpStatusException.class.getName(),
              "com.enofex.taikai.java"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldAllowExceptionUsageOutsideRestrictedLayer() {
      assertDoesNotThrow(() -> Taikai.builder()
          .classes(ThrowsHttpException.class)
          .java(java -> java.noUsageOf(HttpStatusException.class, "com.company.service.."))
          .build().check());
    }

    @Test
    void declarationRuleShouldAllowUncheckedThrowWithoutThrowsClause() {
      assertDoesNotThrow(() -> Taikai.builder()
          .classes(ThrowsHttpException.class)
          .java(java -> java.methodsShouldNotDeclareException(".*", HttpStatusException.class))
          .build().check());
    }
  }

  @Nested
  class NoUsageOfClassBased {

    @Test
    void shouldThrowWhenClassUsesForbiddenType() {
      Taikai taikai = Taikai.builder()
          .classes(UsesArrayList.class)
          .java(java -> java.noUsageOf(ArrayList.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassDoesNotUseForbiddenType() {
      Taikai taikai = Taikai.builder()
          .classes(DoesNotUseArrayList.class)
          .java(java -> java.noUsageOf(ArrayList.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenForbiddenTypeUsedWithinPackage() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importPackages("com.enofex.taikai.java"))
          .java(java -> java.noUsageOf(ArrayList.class, "com.enofex.taikai.java"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenPackageRestrictionDoesNotMatch() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importPackages("com.enofex.taikai.java"))
          .java(java -> java.noUsageOf(ArrayList.class, "com.company.project"))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class NoUsageOfStringBased {

    @Test
    void shouldThrowWhenClassUsesForbiddenTypeByName() {
      Taikai taikai = Taikai.builder()
          .classes(UsesArrayList.class)
          .java(java -> java.noUsageOf("java.util.ArrayList"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassDoesNotUseForbiddenTypeByName() {
      Taikai taikai = Taikai.builder()
          .classes(DoesNotUseArrayList.class)
          .java(java -> java.noUsageOf("java.util.ArrayList"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenForbiddenTypeUsedWithinPackageByName() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importPackages("com.enofex.taikai.java"))
          .java(java -> java.noUsageOf("java.util.ArrayList", "com.enofex.taikai.java"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenPackageRestrictionDoesNotMatchByName() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importPackages("com.enofex.taikai.java"))
          .java(java -> java.noUsageOf("java.util.ArrayList", "com.company.project"))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  static class HttpStatusException extends RuntimeException {
  }

  static class SpecificHttpStatusException extends HttpStatusException {
  }

  static class ThrowsHttpException {
    void execute() {
      throw new HttpStatusException();
    }
  }

  static class ThrowsHttpExceptionSubclass {
    void execute() {
      throw new SpecificHttpStatusException();
    }
  }

  static class UsesArrayList {
    private final List<String> list = new ArrayList<>();

    void add(String value) {
      this.list.add(value);
    }
  }

  static class DoesNotUseArrayList {
    private final String name = "safe";

    void print() {
      System.out.println(this.name);
    }
  }
}
