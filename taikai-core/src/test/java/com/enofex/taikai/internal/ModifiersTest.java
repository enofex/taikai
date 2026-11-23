package com.enofex.taikai.internal;

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
        check("isFinal of a class", JavaClass.class, JavaModifier.FINAL, Modifiers::isFinal),
        check("isFinal of a field", JavaField.class, JavaModifier.FINAL, Modifiers::isFinal),
        check("isFinal of a method", JavaMethod.class, JavaModifier.FINAL, Modifiers::isFinal),
        check("isPrivate of a constructor", JavaConstructor.class, JavaModifier.PRIVATE,
            Modifiers::isPrivate),
        check("isProtected of a method", JavaMethod.class, JavaModifier.PROTECTED,
            Modifiers::isProtected),
        check("isProtected of a field", JavaField.class, JavaModifier.PROTECTED,
            Modifiers::isProtected),
        check("isStatic of a method", JavaMethod.class, JavaModifier.STATIC, Modifiers::isStatic),
        check("isStatic of a field", JavaField.class, JavaModifier.STATIC, Modifiers::isStatic),
        check("isPublic of a field", JavaField.class, JavaModifier.PUBLIC, Modifiers::isPublic),
        check("isPublic of a class", JavaClass.class, JavaModifier.PUBLIC, Modifiers::isPublic),
        check("isPrivate of a class", JavaClass.class, JavaModifier.PRIVATE, Modifiers::isPrivate),
        check("isPrivate of a method", JavaMethod.class, JavaModifier.PRIVATE,
            Modifiers::isPrivate),
        check("isAbstract of a class", JavaClass.class, JavaModifier.ABSTRACT,
            Modifiers::isAbstract),
        check("isAbstract of a method", JavaMethod.class, JavaModifier.ABSTRACT,
            Modifiers::isAbstract),
        check("isSynthetic", HasModifiers.class, JavaModifier.SYNTHETIC, Modifiers::isSynthetic),
        check("isBridge", HasModifiers.class, JavaModifier.BRIDGE, Modifiers::isBridge),
        check("isStatic", HasModifiers.class, JavaModifier.STATIC, Modifiers::isStatic));
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
