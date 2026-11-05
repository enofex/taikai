package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ClassRecordTest {

  @Nested
  class ClassesShouldBeRecords {

    @Test
    void shouldNotThrowWhenClassNameMatchesAndIsRecord() {
      Taikai taikai = Taikai.builder()
          .classes(ValidRecord.class)
          .java(java -> java.classesShouldBeRecords(ClassCriteria.nameMatching(".*ValidRecord")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassNameMatchesAndIsNotRecord() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNonRecord.class)
          .java(java -> java.classesShouldBeRecords(ClassCriteria.nameMatching(".*InvalidNonRecord")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  record ValidRecord(String name, int id) { }

  static class InvalidNonRecord {
    private final String name = "test";
  }
}
