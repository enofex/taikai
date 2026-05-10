package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ImportPatternsTest {

  @Test
  void shouldReturnApacheCommonsPattern() {
    assertNotNull(ImportPatterns.apacheCommons());
  }

  @Test
  void shouldReturnAssertJPattern() {
    assertNotNull(ImportPatterns.assertJ());
  }

  @Test
  void shouldReturnHamcrestPattern() {
    assertNotNull(ImportPatterns.hamcrest());
  }

  @Test
  void shouldReturnHibernatePattern() {
    assertNotNull(ImportPatterns.hibernate());
  }

  @Test
  void shouldReturnJspecifyPattern() {
    assertNotNull(ImportPatterns.jspecify());
  }

  @Test
  void shouldReturnJunitPattern() {
    assertNotNull(ImportPatterns.junit());
  }

  @Test
  void shouldReturnLogbackPattern() {
    assertNotNull(ImportPatterns.logback());
  }

  @Test
  void shouldReturnMockitoPattern() {
    assertNotNull(ImportPatterns.mockito());
  }

  @Test
  void shouldReturnSpringBootPattern() {
    assertNotNull(ImportPatterns.springBoot());
  }

  @Test
  void shouldReturnSpringDataPattern() {
    assertNotNull(ImportPatterns.springData());
  }

  @Test
  void shouldReturnSpringFrameworkPattern() {
    assertNotNull(ImportPatterns.springFramework());
  }

  @Test
  void shouldReturnSpringSecurityPattern() {
    assertNotNull(ImportPatterns.springSecurity());
  }

  @Test
  void shouldReturnTestcontainersPattern() {
    assertNotNull(ImportPatterns.testcontainers());
  }
}
