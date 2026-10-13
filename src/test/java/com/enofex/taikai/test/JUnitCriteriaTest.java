package com.enofex.taikai.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Test;

class JUnitCriteriaTest {

  @Test
  void shouldNotThrowWhenSelectedClassesArePackagePrivate() {
    Taikai taikai = Taikai.builder()
        .classes(PackagePrivateSpec.class, PublicSupport.class)
        .test(test -> test.junit(junit -> junit
            .classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Spec"))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowWhenSelectedClassesArePublic() {
    Taikai taikai = Taikai.builder()
        .classes(PublicSpec.class)
        .test(test -> test.junit(junit -> junit
            .classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Spec"))))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldExcludeInterfaces() {
    Taikai taikai = Taikai.builder()
        .classes(SpecContract.class)
        .test(test -> test.junit(junit -> junit
            .classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Spec.*"))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  static class PackagePrivateSpec {
  }

  public static class PublicSpec {
  }

  public static class PublicSupport {
  }

  public interface SpecContract {
  }
}
