package com.enofex.taikai.java;

import static com.enofex.taikai.internal.Modifiers.isPrivate;
import static com.enofex.taikai.internal.Modifiers.isProtected;
import static com.enofex.taikai.internal.Modifiers.isPublic;
import static com.enofex.taikai.internal.Modifiers.isStatic;
import static com.enofex.taikai.internal.Modifiers.isSynthetic;

import com.enofex.taikai.internal.Classes;
import com.enofex.taikai.internal.Modifiers;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.List;
import java.util.Optional;

final class ProtectedMembers {

  private ProtectedMembers() {
  }

  static ArchCondition<JavaClass> notHaveProtectedMembers() {
    return new ArchCondition<>("not have protected members") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        for (JavaField field : javaClass.getFields()) {
          if (isProtected(field) && !isSynthetic(field)) {
            events.add(SimpleConditionEvent.violated(field,
                "Field %s in final class %s is protected".formatted(
                    field.getName(),
                    javaClass.getName())));
          }
        }

        for (JavaMethod method : javaClass.getMethods()) {
          if (isProtected(method) && !isSynthetic(method) && !overrides(method)) {
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
        .filter(Modifiers::isBridge)
        .map(Classes::bridgeTarget)
        .flatMap(Optional::stream)
        .anyMatch(target -> target.getName().equals(method.getName())
            && names(target.getRawParameterTypes()).equals(parameterTypes));
  }

  private static boolean canBeOverriddenFrom(JavaMethod superMethod, JavaClass owner) {
    if (isPrivate(superMethod) || isStatic(superMethod)) {
      return false;
    }

    return isPublic(superMethod) || isProtected(superMethod)
        || superMethod.getOwner().getPackageName().equals(owner.getPackageName());
  }

  private static List<String> names(List<JavaClass> types) {
    return types.stream()
        .map(JavaClass::getName)
        .toList();
  }
}
