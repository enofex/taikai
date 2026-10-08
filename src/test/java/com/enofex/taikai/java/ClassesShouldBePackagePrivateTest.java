package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ClassesShouldBePackagePrivateTest {

  @Nested
  class ClassesShouldBePackagePrivate {

    @Test
    void shouldNotThrowWhenMatchingClassIsPackagePrivate() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateEntity.class)
          .java(java -> java.classesShouldBePackagePrivate(".*Entity"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingClassIsPublic() {
      Taikai taikai = Taikai.builder()
          .classes(PublicEntity.class)
          .java(java -> java.classesShouldBePackagePrivate(".*Entity"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesAnnotatedWithShouldBePackagePrivate {

    @Test
    void shouldNotThrowWhenAnnotatedClassIsPackagePrivate() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateEntity.class)
          .java(java -> java.classesAnnotatedWithShouldBePackagePrivate(Entity.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassIsPublic() {
      Taikai taikai = Taikai.builder()
          .classes(PublicEntity.class)
          .java(java -> java.classesAnnotatedWithShouldBePackagePrivate(Entity.class.getName()))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesAssignableToShouldBePackagePrivate {

    @Test
    void shouldNotThrowWhenSubtypeIsPackagePrivate() {
      Taikai taikai = Taikai.builder()
          .classes(BaseRepository.class, PackagePrivateRepository.class)
          .java(java -> java.classesAssignableToShouldBePackagePrivate(BaseRepository.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSubtypeIsPublic() {
      Taikai taikai = Taikai.builder()
          .classes(BaseRepository.class, PublicRepository.class)
          .java(java -> java.classesAssignableToShouldBePackagePrivate(
              BaseRepository.class.getName()))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Entity {
  }

  @Entity
  static class PackagePrivateEntity {
  }

  @Entity
  public static class PublicEntity {
  }

  public interface BaseRepository {
  }

  interface PackagePrivateRepository extends BaseRepository {
  }

  public interface PublicRepository extends BaseRepository {
  }
}
