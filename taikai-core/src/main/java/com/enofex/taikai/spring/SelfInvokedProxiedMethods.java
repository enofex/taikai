package com.enofex.taikai.spring;

import static com.enofex.taikai.internal.Classes.methodCallsAndReferences;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaCodeUnitAccess;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.Optional;

final class SelfInvokedProxiedMethods {

  private SelfInvokedProxiedMethods() {
  }

  static ArchCondition<JavaClass> notSelfInvokeMethodsAnnotatedWith(
      Collection<String> annotations) {
    return new ArchCondition<>("not self-invoke methods annotated with %s".formatted(
        String.join(" or ", annotations))) {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        for (JavaCodeUnitAccess<?> call : methodCallsAndReferences(javaClass)) {
          if (!call.getTargetOwner().equals(javaClass)
              || call.getOrigin().getModifiers().contains(JavaModifier.SYNTHETIC)) {
            continue;
          }

          Optional<? extends JavaCodeUnit> target = call.getTarget().resolveMember();

          if (target.isEmpty()) {
            continue;
          }

          JavaCodeUnit method = target.get();

          annotations.stream()
              .filter(method::isMetaAnnotatedWith)
              .findFirst()
              .ifPresent(annotation -> events.add(SimpleConditionEvent.violated(javaClass,
                  "Method %s calls %s in line %d, the self-invocation bypasses the Spring proxy and %s has no effect".formatted(
                      call.getOrigin().getFullName(),
                      method.getFullName(),
                      call.getLineNumber(),
                      annotation))));
        }
      }
    };
  }
}
