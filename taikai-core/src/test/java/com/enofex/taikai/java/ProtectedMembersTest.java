package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Taikai;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProtectedMembersTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(classes = {ValidFinalClass.class, LruCache.class, StringHandler.class})
  void shouldPassWhenFinalClassHasNoProtectedMembersOrOnlyOverridesProtectedMethods(
      Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(JavaConfigurer::finalClassesShouldNotHaveProtectedMembers)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(classes = {FinalClassWithProtectedField.class, FinalClassWithProtectedMethod.class,
      Reset.class})
  void shouldFailWhenFinalClassHasProtectedMembers(Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(JavaConfigurer::finalClassesShouldNotHaveProtectedMembers)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldPassWhenEnumConstantBodyOverridesProtectedMethod() {
    Taikai taikai = Taikai.builder()
        .classes(Operation.class, Operation.PLUS.getClass())
        .java(JavaConfigurer::finalClassesShouldNotHaveProtectedMembers)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldFailForNewProtectedMethodNextToOverride() {
    Taikai taikai = Taikai.builder()
        .classes(LruCacheWithProtectedMethod.class)
        .java(JavaConfigurer::finalClassesShouldNotHaveProtectedMembers)
        .build();

    AssertionError error = assertThrows(AssertionError.class, taikai::check);

    assertTrue(error.getMessage().contains("Method trim in final class"), error.getMessage());
    assertFalse(error.getMessage().contains("Method removeEldestEntry"), error.getMessage());
  }

  static final class ValidFinalClass {

    private int privateField;

    public void publicMethod() {
    }
  }

  static final class FinalClassWithProtectedField {

    protected String protectedField;
  }

  static final class FinalClassWithProtectedMethod {

    protected void protectedMethod() {
    }
  }

  static final class LruCache<K, V> extends LinkedHashMap<K, V> {

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
      return size() > 100;
    }
  }

  static final class LruCacheWithProtectedMethod<K, V> extends LinkedHashMap<K, V> {

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
      return size() > 100;
    }

    protected void trim() {
    }
  }

  abstract static class Handler<T> {

    protected abstract void handle(T value);
  }

  static final class StringHandler extends Handler<String> {

    @Override
    protected void handle(String value) {
    }
  }

  enum Operation {
    PLUS {
      @Override
      protected int apply(int left, int right) {
        return left + right;
      }
    };

    protected abstract int apply(int left, int right);
  }

  static class Resettable {

    private void reset() {
    }
  }

  static final class Reset extends Resettable {

    protected void reset() {
    }
  }
}
