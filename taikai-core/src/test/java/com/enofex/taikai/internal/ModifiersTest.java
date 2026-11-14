package com.enofex.taikai.internal;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;
import static com.tngtech.archunit.core.domain.JavaModifier.PROTECTED;
import static com.tngtech.archunit.core.domain.JavaModifier.PUBLIC;
import static com.tngtech.archunit.core.domain.JavaModifier.STATIC;
import static com.tngtech.archunit.core.domain.JavaModifier.SYNTHETIC;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.properties.HasModifiers;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ModifiersTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("modifierChecks")
  void shouldMatchOnlyElementsWithModifier(ModifierCheck<?> check) {
    assertTrue(check.matches(Set.of(check.modifier())));
    assertFalse(check.matches(Set.of()));
  }

  static Stream<Named<ModifierCheck<?>>> modifierChecks() {
    return Stream.of(
        check("isClassFinal", JavaClass.class, FINAL, Modifiers::isClassFinal),
        check("isConstructorPrivate", JavaConstructor.class, PRIVATE,
            Modifiers::isConstructorPrivate),
        check("isMethodProtected", JavaMethod.class, PROTECTED, Modifiers::isMethodProtected),
        check("isMethodStatic", JavaMethod.class, STATIC, Modifiers::isMethodStatic),
        check("isFieldStatic", JavaField.class, STATIC, Modifiers::isFieldStatic),
        check("isFieldPublic", JavaField.class, PUBLIC, Modifiers::isFieldPublic),
        check("isFieldProtected", JavaField.class, PROTECTED, Modifiers::isFieldProtected),
        check("isFieldFinal", JavaField.class, FINAL, Modifiers::isFieldFinal),
        check("isSynthetic", HasModifiers.class, SYNTHETIC, Modifiers::isSynthetic),
        check("isStatic", HasModifiers.class, STATIC, Modifiers::isStatic));
  }

  private static <T extends HasModifiers> Named<ModifierCheck<?>> check(String name,
      Class<T> type, JavaModifier modifier, Predicate<T> predicate) {
    return Named.of(name, new ModifierCheck<>(type, modifier, predicate));
  }

  private record ModifierCheck<T extends HasModifiers>(Class<T> type, JavaModifier modifier,
      Predicate<T> predicate) {

    boolean matches(Set<JavaModifier> modifiers) {
      T element = mock(this.type);
      when(element.getModifiers()).thenReturn(modifiers);

      return this.predicate.test(element);
    }
  }
}
