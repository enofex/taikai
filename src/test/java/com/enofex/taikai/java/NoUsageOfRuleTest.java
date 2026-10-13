package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.MethodCriteria;
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
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.enofex.taikai.java"),
              HttpStatusException.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldRejectExceptionSubclassesByTypeName() {
      Taikai taikai = Taikai.builder()
          .classes(ThrowsHttpExceptionSubclass.class)
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.enofex.taikai.java"),
              HttpStatusException.class.getName()))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldAllowExceptionUsageOutsideRestrictedLayer() {
      assertDoesNotThrow(() -> Taikai.builder()
          .classes(ThrowsHttpException.class)
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.company.service.."),
              HttpStatusException.class))
          .build().check());
    }

    @Test
    void declarationRuleShouldAllowUncheckedThrowWithoutThrowsClause() {
      assertDoesNotThrow(() -> Taikai.builder()
          .classes(ThrowsHttpException.class)
          .java(java -> java.methodsShouldNotDeclareException(MethodCriteria.nameMatching(".*"), HttpStatusException.class))
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
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.enofex.taikai.java"),
              ArrayList.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenPackageRestrictionDoesNotMatch() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importPackages("com.enofex.taikai.java"))
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.company.project"), ArrayList.class))
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
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.enofex.taikai.java"),
              "java.util.ArrayList"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenPackageRestrictionDoesNotMatchByName() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importPackages("com.enofex.taikai.java"))
          .java(java -> java.noUsageOf(ClassCriteria.inPackage("com.company.project"),
              "java.util.ArrayList"))
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
