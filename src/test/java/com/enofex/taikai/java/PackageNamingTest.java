package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Test;

class PackageNamingTest {

  @Test
  void shouldApplyPackageNamingConvention() {
    Taikai taikai = Taikai.builder()
        .classes(ValidPackageClass.class)
        .java(java -> java.naming(naming -> naming.packagesShouldMatch("com\\.enofex\\..*")))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowWhenPackageDoesNotMatchConvention() {
    Taikai taikai = Taikai.builder()
        .classes(ValidPackageClass.class)
        .java(java -> java.naming(naming -> naming.packagesShouldMatch("org\\.enofex\\..*")))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }


  static class ValidPackageClass {

  }
}
