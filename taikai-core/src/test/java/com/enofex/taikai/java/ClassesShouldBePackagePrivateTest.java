package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ClassesShouldBePackagePrivateTest {

  @Nested
  class ClassesShouldBePackagePrivate {

    @Test
    void shouldNotThrowWhenMatchingClassIsPackagePrivate() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateEntity.class)
          .java(java -> java.classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Entity")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingClassIsPublic() {
      Taikai taikai = Taikai.builder()
          .classes(PublicEntity.class)
          .java(java -> java.classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Entity")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  static class PackagePrivateEntity {
  }

  public static class PublicEntity {
  }
}
