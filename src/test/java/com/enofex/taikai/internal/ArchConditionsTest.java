package com.enofex.taikai.internal;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.ThrowsClause;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArchConditionsTest {

  @Mock
  private JavaMethod mockMethod;
  @Mock
  private JavaField mockField;
  @Mock
  private JavaClass mockClass;
  @Mock
  private ConditionEvents events;
  @Mock
  private ThrowsClause mockThrowsClause;


  @Test
  void shouldNotDeclareThrownExceptions() {
    when(this.mockMethod.getThrowsClause()).thenReturn(this.mockThrowsClause);
    when(this.mockThrowsClause.isEmpty()).thenReturn(true);

    ArchConditions.notDeclareThrownExceptions().check(this.mockMethod, this.events);

    verify(this.events, never()).add(any(SimpleConditionEvent.class));
  }

  @Test
  void shouldDeclareThrownExceptions() {
    when(this.mockMethod.getThrowsClause()).thenReturn(this.mockThrowsClause);
    when(this.mockThrowsClause.isEmpty()).thenReturn(false);

    ArchConditions.notDeclareThrownExceptions().check(this.mockMethod, this.events);

    verify(this.events).add(any(SimpleConditionEvent.class));
  }

  @Test
  void shouldBePublicButNotStatic() {
    when(this.mockField.getName()).thenReturn("publicField");
    when(this.mockField.getOwner()).thenReturn(this.mockClass);
    when(this.mockField.getModifiers()).thenReturn(EnumSet.of(JavaModifier.PUBLIC));

    ArchConditions.notBePublicUnlessStatic().check(this.mockField, this.events);

    ArgumentCaptor<SimpleConditionEvent> eventCaptor = ArgumentCaptor.forClass(
        SimpleConditionEvent.class);
    verify(this.events).add(eventCaptor.capture());
    assertEquals("Field %s in class %s is public".formatted(
            this.mockField.getName(), this.mockClass.getFullName()),
        eventCaptor.getValue().getDescriptionLines().get(0));
  }


  @Test
  void shouldHaveRequiredModifiers() {
    Set<JavaModifier> requiredModifiers = EnumSet.of(JavaModifier.PRIVATE, JavaModifier.FINAL);
    when(this.mockField.getModifiers()).thenReturn(requiredModifiers);

    ArchConditions.hasFieldModifiers(requiredModifiers).check(this.mockField, this.events);

    verify(this.events, never()).add(any(SimpleConditionEvent.class));
  }

  @Test
  void shouldNotHaveRequiredModifiers() {
    Set<JavaModifier> requiredModifiers = EnumSet.of(JavaModifier.PRIVATE, JavaModifier.FINAL);
    when(this.mockField.getModifiers()).thenReturn(EnumSet.of(JavaModifier.PUBLIC));
    when(this.mockField.getName()).thenReturn("field");
    when(this.mockField.getOwner()).thenReturn(this.mockClass);

    ArchConditions.hasFieldModifiers(requiredModifiers).check(this.mockField, this.events);

    ArgumentCaptor<SimpleConditionEvent> eventCaptor = ArgumentCaptor.forClass(
        SimpleConditionEvent.class);
    verify(this.events).add(eventCaptor.capture());
    assertEquals("Field %s in class %s is missing one of this %s modifier".formatted(
            this.mockField.getName(),
            this.mockClass.getFullName(),
            "PRIVATE, FINAL"),
        eventCaptor.getValue().getDescriptionLines().get(0));
  }

  @Test
  void shouldHaveFieldOfType() {
    String typeName = "com.example.MyType";
    JavaClass mockRawType = mock(JavaClass.class);

    when(mockRawType.getName()).thenReturn(typeName);
    when(this.mockField.getRawType()).thenReturn(mockRawType);
    when(this.mockClass.getAllFields()).thenReturn(Collections.singleton(this.mockField));

    ArchConditions.haveFieldOfType(typeName).check(this.mockClass, this.events);

    verify(this.events, never()).add(any(SimpleConditionEvent.class));
  }

  @Test
  void shouldNotHaveFieldOfType() {
    String typeName = "com.example.MyType";
    JavaClass mockRawType = mock(JavaClass.class);

    when(mockRawType.getName()).thenReturn("com.example.AnotherType");
    when(this.mockField.getRawType()).thenReturn(mockRawType);
    when(this.mockClass.getAllFields()).thenReturn(Collections.singleton(this.mockField));

    ArchConditions.haveFieldOfType(typeName).check(this.mockClass, this.events);

    verify(this.events).add(any(SimpleConditionEvent.class));
  }

  @Test
  void shouldHaveRequiredMethodModifiers() {
    Set<JavaModifier> requiredModifiers = EnumSet.of(JavaModifier.PUBLIC, JavaModifier.STATIC);
    when(this.mockMethod.getModifiers()).thenReturn(requiredModifiers);

    ArchConditions.hasMethodsModifiers(requiredModifiers).check(this.mockMethod, this.events);

    verify(this.events, never()).add(any(SimpleConditionEvent.class));
  }

  @Test
  void shouldNotHaveRequiredMethodModifiers() {
    Set<JavaModifier> requiredModifiers = EnumSet.of(JavaModifier.PUBLIC, JavaModifier.STATIC);
    when(this.mockMethod.getModifiers()).thenReturn(EnumSet.of(JavaModifier.PUBLIC));
    when(this.mockMethod.getName()).thenReturn("someMethod");
    when(this.mockMethod.getOwner()).thenReturn(this.mockClass);

    ArchConditions.hasMethodsModifiers(requiredModifiers).check(this.mockMethod, this.events);

    ArgumentCaptor<SimpleConditionEvent> eventCaptor = ArgumentCaptor.forClass(SimpleConditionEvent.class);
    verify(this.events).add(eventCaptor.capture());
    assertEquals("Method %s in class %s is missing one of this %s modifier".formatted(
            this.mockMethod.getName(),
            this.mockClass.getFullName(),
            "PUBLIC, STATIC"),
        eventCaptor.getValue().getDescriptionLines().get(0));
  }

  @Test
  void shouldHaveRequiredClassModifiers() {
    Set<JavaModifier> requiredModifiers = EnumSet.of(JavaModifier.PUBLIC, JavaModifier.FINAL);
    when(this.mockClass.getModifiers()).thenReturn(requiredModifiers);

    ArchConditions.hasClassModifiers(requiredModifiers).check(this.mockClass, this.events);

    verify(this.events, never()).add(any(SimpleConditionEvent.class));
  }

  @Test
  void shouldNotAddEventWhenMethodDoesNotHaveForbiddenModifiers() {
    Set<JavaModifier> forbiddenModifiers = EnumSet.of(JavaModifier.STATIC);
    when(this.mockMethod.getModifiers()).thenReturn(EnumSet.of(JavaModifier.PUBLIC));

    ArchConditions.notHasMethodModifiers(forbiddenModifiers).check(this.mockMethod, this.events);

    verify(this.events, never()).add(any(SimpleConditionEvent.class));
  }

  @Test
  void shouldAddEventWhenMethodHasForbiddenModifiers() {
    Set<JavaModifier> forbiddenModifiers = EnumSet.of(JavaModifier.STATIC);
    when(this.mockMethod.getModifiers()).thenReturn(EnumSet.of(JavaModifier.PUBLIC, JavaModifier.STATIC));
    when(this.mockMethod.getName()).thenReturn("forbiddenMethod");
    when(this.mockMethod.getOwner()).thenReturn(this.mockClass);

    ArchConditions.notHasMethodModifiers(forbiddenModifiers).check(this.mockMethod, this.events);

    verify(this.events, times(1)).add(any(SimpleConditionEvent.class));
  }

  @Test
  void shouldNotHaveRequiredClassModifiers() {
    Set<JavaModifier> requiredModifiers = EnumSet.of(JavaModifier.PUBLIC, JavaModifier.FINAL);
    when(this.mockClass.getModifiers()).thenReturn(EnumSet.of(JavaModifier.PUBLIC));
    when(this.mockClass.getName()).thenReturn("MyClass");

    ArchConditions.hasClassModifiers(requiredModifiers).check(this.mockClass, this.events);

    ArgumentCaptor<SimpleConditionEvent> eventCaptor = ArgumentCaptor.forClass(SimpleConditionEvent.class);
    verify(this.events).add(eventCaptor.capture());
    assertEquals("Class %s is missing one of this %s modifier".formatted(
            this.mockClass.getName(),
            "PUBLIC, FINAL"),
        eventCaptor.getValue().getDescriptionLines().get(0));
  }
}
