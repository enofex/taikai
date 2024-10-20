package com.enofex.taikai.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DescribedPredicatesTest {

  @Mock
  private CanBeAnnotated canBeAnnotated;
  @Mock
  private JavaClass javaClass;

  @Test
  void shouldReturnTrueWhenAnnotatedWithSpecificAnnotation() {
    String annotation = "MyAnnotation";
    when(this.canBeAnnotated.isAnnotatedWith(annotation)).thenReturn(true);

    assertTrue(DescribedPredicates.annotatedWith(annotation, false).test(this.canBeAnnotated));
  }

  @Test
  void shouldReturnFalseWhenNotAnnotatedWithSpecificAnnotation() {
    String annotation = "MyAnnotation";
    when(this.canBeAnnotated.isAnnotatedWith(annotation)).thenReturn(false);

    assertFalse(DescribedPredicates.annotatedWith(annotation, false).test(this.canBeAnnotated));
  }

  @Test
  void shouldReturnTrueWhenAnnotatedWithAllAnnotations() {
    Set<String> annotations = Set.of("MyAnnotation1", "MyAnnotation2");

    when(this.canBeAnnotated.isAnnotatedWith("MyAnnotation1")).thenReturn(true);
    when(this.canBeAnnotated.isAnnotatedWith("MyAnnotation2")).thenReturn(true);

    assertTrue(DescribedPredicates.annotatedWithAll(annotations, false).test(this.canBeAnnotated));
  }

  @Test
  void shouldReturnFalseWhenNotAnnotatedWithAllAnnotations() {
    Set<String> annotations = Set.of("MyAnnotation1", "MyAnnotation2");

    when(this.canBeAnnotated.isAnnotatedWith("MyAnnotation1")).thenReturn(false);
    when(this.canBeAnnotated.isAnnotatedWith("MyAnnotation2")).thenReturn(true);

    assertFalse(DescribedPredicates.annotatedWithAll(annotations, false).test(this.canBeAnnotated));
  }

  @Test
  void shouldReturnTrueWhenClassIsFinal() {
    when(this.javaClass.getModifiers()).thenReturn(
        Set.of(com.tngtech.archunit.core.domain.JavaModifier.FINAL));

    assertTrue(DescribedPredicates.areFinal().test(this.javaClass));
  }

  @Test
  void shouldReturnFalseWhenClassIsNotFinal() {
    when(this.javaClass.getModifiers()).thenReturn(Collections.emptySet());

    assertFalse(DescribedPredicates.areFinal().test(this.javaClass));
  }
}
