package com.enofex.taikai.java;

import static com.enofex.taikai.internal.Modifiers.isFieldProtected;
import static com.enofex.taikai.internal.Modifiers.isFieldSynthetic;
import static com.enofex.taikai.internal.Modifiers.isMethodProtected;
import static com.enofex.taikai.internal.Modifiers.isMethodSynthetic;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.List;
import java.util.Set;

final class ProtectedMembers {

  private ProtectedMembers() {
  }

  static ArchCondition<JavaClass> notHaveProtectedMembers() {
    return new ArchCondition<>("not have protected members") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        for (JavaField field : javaClass.getFields()) {
          if (isFieldProtected(field) && !isFieldSynthetic(field)) {
            events.add(SimpleConditionEvent.violated(field,
                "Field %s in final class %s is protected".formatted(
                    field.getName(),
                    javaClass.getName())));
          }
        }

        for (JavaMethod method : javaClass.getMethods()) {
          if (isMethodProtected(method) && !isMethodSynthetic(method) && !overrides(method)) {
            events.add(SimpleConditionEvent.violated(method,
                "Method %s in final class %s is protected".formatted(
                    method.getName(),
                    javaClass.getName())));
          }
        }
      }
    };
  }

  /**
   * Returns whether a method overrides a method of a superclass, such as
   * {@code LinkedHashMap.removeEldestEntry}, which it cannot make private. An override of a generic
   * method whose erasure differs is recognized by the bridge method that the compiler generates for
   * it.
   */
  private static boolean overrides(JavaMethod method) {
    JavaClass owner = method.getOwner();
    List<String> parameterTypes = names(method.getRawParameterTypes());

    boolean overridesSuperclassMethod = owner.getAllRawSuperclasses().stream()
        .flatMap(superclass -> superclass.getMethods().stream())
        .anyMatch(superMethod -> superMethod.getName().equals(method.getName())
            && names(superMethod.getRawParameterTypes()).equals(parameterTypes)
            && canBeOverriddenFrom(superMethod, owner));

    return overridesSuperclassMethod || owner.getMethods().stream()
        .filter(bridge -> bridge.getModifiers().contains(JavaModifier.BRIDGE))
        .flatMap(bridge -> bridge.getMethodCallsFromSelf().stream())
        .anyMatch(call -> call.getTargetOwner().equals(owner)
            && call.getName().equals(method.getName())
            && names(call.getTarget().getRawParameterTypes()).equals(parameterTypes));
  }

  private static boolean canBeOverriddenFrom(JavaMethod superMethod, JavaClass owner) {
    Set<JavaModifier> modifiers = superMethod.getModifiers();

    if (modifiers.contains(JavaModifier.PRIVATE) || modifiers.contains(JavaModifier.STATIC)) {
      return false;
    }

    return modifiers.contains(JavaModifier.PUBLIC) || modifiers.contains(JavaModifier.PROTECTED)
        || superMethod.getOwner().getPackageName().equals(owner.getPackageName());
  }

  private static List<String> names(List<JavaClass> types) {
    return types.stream()
        .map(JavaClass::getName)
        .toList();
  }
}
