package com.enofex.taikai.internal;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModifiersTest {

  @Mock
  private JavaClass javaClass;
  @Mock
  private JavaConstructor constructor;
  @Mock
  private JavaField field;
  @Mock
  private JavaMethod method;


  @Test
  void shouldReturnTrueWhenClassIsFinal() {
    when(this.javaClass.getModifiers()).thenReturn(Set.of(JavaModifier.FINAL));

    assertTrue(Modifiers.isClassFinal(this.javaClass));
  }

  @Test
  void shouldReturnFalseWhenClassIsNotFinal() {
    when(this.javaClass.getModifiers()).thenReturn(Collections.emptySet());

    assertFalse(Modifiers.isClassFinal(this.javaClass));
  }

  @Test
  void shouldReturnTrueWhenConstructorIsPrivate() {
    when(this.constructor.getModifiers()).thenReturn(Set.of(JavaModifier.PRIVATE));

    assertTrue(Modifiers.isConstructorPrivate(this.constructor));
  }

  @Test
  void shouldReturnFalseWhenConstructorIsNotPrivate() {
    when(this.constructor.getModifiers()).thenReturn(Collections.emptySet());

    assertFalse(Modifiers.isConstructorPrivate(this.constructor));
  }

  @Test
  void shouldReturnTrueWhenMethodIsProtected() {
    when(this.method.getModifiers()).thenReturn(Set.of(JavaModifier.PROTECTED));

    assertTrue(Modifiers.isMethodProtected(this.method));
  }

  @Test
  void shouldReturnFalseWhenMethodIsNotProtected() {
    when(this.method.getModifiers()).thenReturn(Collections.emptySet());

    assertFalse(Modifiers.isMethodProtected(this.method));
  }

  @Test
  void shouldReturnTrueWhenMethodIsStatic() {
    when(this.method.getModifiers()).thenReturn(Set.of(JavaModifier.STATIC));

    assertTrue(Modifiers.isMethodStatic(this.method));
  }

  @Test
  void shouldReturnFalseWhenMethodIsNotStatic() {
    when(this.method.getModifiers()).thenReturn(Collections.emptySet());

    assertFalse(Modifiers.isMethodStatic(this.method));
  }

  @Test
  void shouldReturnTrueWhenFieldIsStatic() {
    when(this.field.getModifiers()).thenReturn(Set.of(JavaModifier.STATIC));

    assertTrue(Modifiers.isFieldStatic(this.field));
  }

  @Test
  void shouldReturnFalseWhenFieldIsNotStatic() {
    when(this.field.getModifiers()).thenReturn(Collections.emptySet());

    assertFalse(Modifiers.isFieldStatic(this.field));
  }

  @Test
  void shouldReturnTrueWhenFieldIsPublic() {
    when(this.field.getModifiers()).thenReturn(Set.of(JavaModifier.PUBLIC));

    assertTrue(Modifiers.isFieldPublic(this.field));
  }

  @Test
  void shouldReturnFalseWhenFieldIsNotPublic() {
    when(this.field.getModifiers()).thenReturn(Collections.emptySet());

    assertFalse(Modifiers.isFieldPublic(this.field));
  }

  @Test
  void shouldReturnTrueWhenFieldIsProtected() {
    when(this.field.getModifiers()).thenReturn(Set.of(JavaModifier.PROTECTED));

    assertTrue(Modifiers.isFieldProtected(this.field));
  }

  @Test
  void shouldReturnFalseWhenFieldIsNotProtected() {
    when(this.field.getModifiers()).thenReturn(Collections.emptySet());

    assertFalse(Modifiers.isFieldProtected(this.field));
  }

  @Test
  void shouldReturnTrueWhenFieldIsFinal() {
    when(this.field.getModifiers()).thenReturn(Set.of(JavaModifier.FINAL));

    assertTrue(Modifiers.isFieldFinal(this.field));
  }

  @Test
  void shouldReturnFalseWhenFieldIsNotFinal() {
    when(this.field.getModifiers()).thenReturn(Collections.emptySet());

    assertFalse(Modifiers.isFieldFinal(this.field));
  }

  @Test
  void shouldReturnTrueWhenFieldIsSynthetic() {
    when(this.field.getModifiers()).thenReturn(Set.of(JavaModifier.SYNTHETIC));

    assertTrue(Modifiers.isFieldSynthetic(this.field));
  }

  @Test
  void shouldReturnFalseWhenFieldIsNotSynthetic() {
    when(this.field.getModifiers()).thenReturn(Collections.emptySet());

    assertFalse(Modifiers.isFieldSynthetic(this.field));
  }
}
